package muzusi.infrastructure.kis.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.kis.aop.KisRateLimit;
import muzusi.infrastructure.kis.constant.KisUrlConstant;
import muzusi.infrastructure.kis.exception.KisOAuthApiException;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class KisOAuthClient {
    private final KisProperties kisProperties;
    
    /**
     * 한국투자증권 접근 토큰 발급 요청 메서드
     *
     * @param appKey    한국투자증권 앱키
     * @param appSecret 한국투자증권 앱시크릿
     * @return          한국투자증권 접근 토큰 {@code "type value"} (ex. {@code "Bearer xxx"})
     */
    @KisRateLimit
    public String getAccessToken(String appKey, String appSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<KisAccessTokenRequest> request = new HttpEntity<>(KisAccessTokenRequest.from(appKey, appSecret), headers);
        
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            KisAccessTokenResponse response = restTemplate.exchange(
                    kisProperties.getUrl(KisUrlConstant.ACCESS_TOKEN_ISSUE),
                    HttpMethod.POST,
                    request,
                    KisAccessTokenResponse.class
            ).getBody();
            
            return response.resolveAccessToken();
        } catch (Exception e) {
            throw new KisOAuthApiException("한국투자증권 접근 토큰 발급 API 호출 중 에러가 발생하였습니다.", e);
        }
    }
    
    /**
     * 한국투자증권 웹소켓 접속키 발급 메서드
     *
     * @param appKey    한국투자증권 앱키
     * @param appSecret 한국투자증권 앱시크릿
     * @return          한국투자증권 웹소켓 접속키
     */
    @KisRateLimit
    public String getWebSocketKey(String appKey, String appSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        KisWebSocketKeyRequest body = KisWebSocketKeyRequest.from(appKey, appSecret);
        
        HttpEntity<KisWebSocketKeyRequest> request = new HttpEntity<>(body, headers);
        
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            KisWebSocketKeyResponse response = restTemplate.exchange(
                    kisProperties.getUrl(KisUrlConstant.WEBSOCKET_KEY_ISSUE),
                    HttpMethod.POST,
                    request,
                    KisWebSocketKeyResponse.class
            ).getBody();
            
            return response.webSocketKey();
        } catch (Exception e) {
            throw new KisOAuthApiException("한국투자증권 웹소켓 접속키 발급 API 호출 중 에러가 발생하였습니다.", e);
        }
    }
    
    private record KisAccessTokenRequest(
            @JsonProperty(value = "grant_type") String grantType,
            @JsonProperty(value = "appkey") String appKey,
            @JsonProperty(value = "appsecret") String appSecret
    ) {
        public static KisAccessTokenRequest from(String appKey, String appSecret) {
            return new KisAccessTokenRequest("client_credentials", appKey, appSecret);
        }
    }
    
    private record KisWebSocketKeyRequest(
            @JsonProperty(value = "grant_type") String grantType,
            @JsonProperty(value = "appkey") String appKey,
            @JsonProperty(value = "secretkey") String appSecret
    ) {
        public static KisWebSocketKeyRequest from(String appKey, String appSecret) {
            return new KisWebSocketKeyRequest("client_credentials", appKey, appSecret);
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KisAccessTokenResponse(
            @JsonProperty(value = "access_token") String accessToken,
            @JsonProperty(value = "token_type") String tokenType
    ) {
        private String resolveAccessToken() {
            return tokenType + " " + accessToken;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KisWebSocketKeyResponse(
            @JsonProperty(value = "approval_key") String webSocketKey
    ) { }
}