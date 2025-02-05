package muzusi.infrastructure.kis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import muzusi.application.kis.dto.KisAuthDto;
import muzusi.application.stock.dto.StockPriceDto;
import muzusi.global.exception.KisApiException;
import muzusi.global.redis.RedisService;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class KisStockClient {
    private final KisProperties kisProperties;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;

    public StockPriceDto getStockPrice(String code) {
        HttpHeaders headers = new HttpHeaders();
        KisAuthDto.AccessToken accessToken = (KisAuthDto.AccessToken) redisService.get(KisConstant.ACCESS_TOKEN_PREFIX.getValue());
        headers.add("authorization", accessToken.getValue());
        headers.add("appkey", kisProperties.getAppKey());
        headers.add("appsecret", kisProperties.getAppSecret());
        headers.add("tr_id", "FHKST01010100");

        String uri = UriComponentsBuilder.fromUriString(kisProperties.getUrl(KisUrlConstant.INQUIRE_PRICE))
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_INPUT_ISCD", code)
                .build().toUriString();

        HttpEntity<String> requestInfo = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestInfo,
                    String.class
            );

            JsonNode output = objectMapper.readTree(response.getBody()).get("output");

            return StockPriceDto.builder()
                    .code(code)
                    .price(output.get("stck_prpr").asLong())
                    .time(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            throw new KisApiException(e);
        }
    }
}