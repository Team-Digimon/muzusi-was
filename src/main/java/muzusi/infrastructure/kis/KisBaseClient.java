package muzusi.infrastructure.kis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.kis.dto.KisAuthDto;
import muzusi.global.redis.RedisService;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisBaseClient {
    private final KisProperties kisProperties;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;

    public String getTradingValue(int code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        KisAuthDto.AccessToken accessToken = (KisAuthDto.AccessToken) redisService.get(KisConstant.ACCESS_TOKEN_PREFIX.getValue());
        headers.add("authorization", accessToken.getValue());
        headers.add("appkey", kisProperties.getAppKey());
        headers.add("appsecret", kisProperties.getAppSecret());
        headers.add("tr_id", "FHKST01010100 ");
        headers.add("custtype", "P");

        String uri = UriComponentsBuilder.fromUriString(kisProperties.getUrl(KisUrlConstant.INQUIRE_PRICE))
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_INPUT_ISCD", String.valueOf(code))
                .build()
                .toUriString();

        HttpEntity<String> requestInfo = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestInfo,
                    String.class
            );

            JsonNode rootNode = objectMapper.readTree(response.getBody());

            return rootNode.get("output").get("acml_tr_pbmn").asText();
        } catch (Exception e) {
            log.error("[KIS ERROR] " + e.getMessage());
            return null;
        }
    }
}