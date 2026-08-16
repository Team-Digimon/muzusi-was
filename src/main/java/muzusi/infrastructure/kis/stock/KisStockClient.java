package muzusi.infrastructure.kis.stock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import muzusi.global.exception.KisApiException;
import muzusi.infrastructure.kis.KisRequestFactory;
import muzusi.infrastructure.kis.constant.KisUrlConstant;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class KisStockClient {
    private final KisProperties kisProperties;
    private final ObjectMapper objectMapper;
    private final KisRequestFactory kisRequestFactory;
    
    private static final String INQUIRE_PRICE_TR_ID = "FHKST01010100";

    public Long getStockInquirePrice(String stockCode) {
        HttpHeaders headers = kisRequestFactory.getHttpHeader(INQUIRE_PRICE_TR_ID);

        String uri = UriComponentsBuilder.fromUriString(kisProperties.getUrl(KisUrlConstant.INQUIRE_PRICE))
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_INPUT_ISCD", stockCode)
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

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode output = rootNode.get("output");

            return output.get("stck_prpr").asLong();
        } catch (Exception e) {
            throw new KisApiException(e);
        }
    }
}