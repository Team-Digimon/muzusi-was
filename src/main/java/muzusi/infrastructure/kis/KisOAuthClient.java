package muzusi.infrastructure.kis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.global.exception.KisApiException;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisOAuthClient {
    private final KisProperties kisProperties;
    private final ObjectMapper objectMapper;

    /**
     * 한국투자증권 접근 토큰 발급 메서드
     * @return String : 한국투자증권 API 접속토큰
     */
    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", kisProperties.getAppKey());
        body.put("appsecret", kisProperties.getAppSecret());

        HttpEntity<Map<String, String>> requestInfo = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        try{
            ResponseEntity<String> response = restTemplate.exchange(
                    kisProperties.getUrl(KisUrlConstant.ACCESS_TOKEN_ISSUE),
                    HttpMethod.POST,
                    requestInfo,
                    String.class
            );

            JsonNode rootNode = objectMapper.readTree(response.getBody());

            return rootNode.path("token_type").asText() + " "  + rootNode.path("access_token").asText();
        } catch (Exception e){
            throw new KisApiException(e);
        }
    }

    /**
     * 한국투자증권 웹소켓 접속키 발급 메서드
     * @return String : 한국투자증권 웹소켓 접속키
     */
    public String getWebSocketKey() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", kisProperties.getAppKey());
        body.put("secretkey", kisProperties.getAppSecret());

        HttpEntity<Map<String, String>> requestInfo = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        try{
            ResponseEntity<String> response = restTemplate.exchange(
                    kisProperties.getUrl(KisUrlConstant.WEBSOCKET_KEY_ISSUE),
                    HttpMethod.POST,
                    requestInfo,
                    String.class
            );

            JsonNode rootNode = objectMapper.readTree(response.getBody());

            return rootNode.path("approval_key").asText();
        } catch (Exception e){
            throw new KisApiException(e);
        }
    }
}