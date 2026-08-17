package muzusi.infrastructure.kis.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.kis.exception.KisOAuthApiException;
import muzusi.infrastructure.kis.constant.KisUrlConstant;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KisOAuthClient {
    private final KisProperties kisProperties;
    private final ObjectMapper objectMapper;
    
    private static final String ACCESS_TOKEN_FORMAT = "%s %s";
    
    /**
     * 한국투자증권 접근 토큰 발급 요청 메서드
     *
     * @param appKey    한국투자증권 앱키
     * @param appSecret 한국투자증권 앱시크릿
     * @return          한국투자증권 접근 토큰 {@code "type value"} (ex. {@code "Bearer xxx"})
     */
    public String getAccessToken(String appKey, String appSecret) {
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", appKey);
        body.put("appsecret", appSecret);
        
        JsonNode rootNode = requestCredential(kisProperties.getUrl(KisUrlConstant.ACCESS_TOKEN_ISSUE), body);
        
        String tokenType = rootNode.path("token_type").asText();
        String tokenValue = rootNode.path("access_token").asText();
        
        return ACCESS_TOKEN_FORMAT.formatted(tokenType, tokenValue);
    }
    
    /**
     * 한국투자증권 웹소켓 접속키 발급 메서드
     *
     * @param appKey    한국투자증권 앱키
     * @param appSecret 한국투자증권 앱시크릿
     * @return          한국투자증권 웹소켓 접속키
     */
    public String getWebSocketKey(String appKey, String appSecret) {
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", appKey);
        body.put("secretkey", appSecret);
        
        JsonNode rootNode = requestCredential(kisProperties.getUrl(KisUrlConstant.WEBSOCKET_KEY_ISSUE), body);
        
        return rootNode.path("approval_key").asText();
    }
    
    /**
     * 한국투자증권 인증키 발급 요청 메서드
     *
     * @param url       요청 URL
     * @return          응답을 {@link JsonNode}로 파싱한 결과
     */
    private JsonNode requestCredential(String url, Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestInfo = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestInfo,
                    String.class
            );

            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new KisOAuthApiException(e);
        }
    }
}