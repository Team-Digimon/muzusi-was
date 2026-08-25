package muzusi.infrastructure.market.client.kis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.infrastructure.kis.aop.KisRateLimit;
import muzusi.infrastructure.kis.exception.KisApiException;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisMarketOpenClient {
    private final ObjectMapper objectMapper;
    private final KisProperties kisProperties;
    private final KisRequestFactory kisRequestFactory;
    
    private static final String TR_ID = "CTCA0903R";
    
    @KisRateLimit
    public boolean isMarketOpen() {
        HttpHeaders header = kisRequestFactory.getHttpHeader(TR_ID);
        
        HttpEntity<Map<String, String>> requestInfo = new HttpEntity<>(header);
        
        String uri = UriComponentsBuilder.fromUriString(kisProperties.getUrl(KisUrlConstant.MARKET_OPEN))
                .queryParam("BASS_DT", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE))
                .queryParam("CTX_AREA_NK", "")
                .queryParam("CTX_AREA_FK", "")
                .toUriString();
        
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestInfo,
                    String.class
            );
            
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String result = extractIsMarketOpenResult(rootNode);
            return isMarketOpen(result);
        } catch (Exception e) {
            log.error("[Error] Error occurred during KisMarketOpen");
            throw new KisApiException(e);
        }
    }
    
    private boolean isMarketOpen(String text) {
        return "Y".equals(text);
    }
    
    private String extractIsMarketOpenResult(JsonNode rootNode) {
        return rootNode.get("output").get(0).get("opnd_yn").asText();
    }
}
