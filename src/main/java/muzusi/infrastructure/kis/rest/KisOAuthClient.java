package muzusi.infrastructure.kis.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import muzusi.global.exception.KisOAuthApiException;
import muzusi.infrastructure.kis.constant.KisUrlConstant;
import muzusi.infrastructure.properties.KisProperties;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KisOAuthClient {
    private final KisProperties kisProperties;
    private final ObjectMapper objectMapper;

    /**
     * 한국투자증권 접근 토큰 발급 메서드
     *
     * @return String : 한국투자증권 API 접속토큰
     */
    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", kisProperties.getAppKey().get(0));
        body.put("appsecret", kisProperties.getAppSecret().get(0));

        HttpEntity<Map<String, String>> requestInfo = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    kisProperties.getUrl(KisUrlConstant.ACCESS_TOKEN_ISSUE),
                    HttpMethod.POST,
                    requestInfo,
                    String.class
            );

            JsonNode rootNode = objectMapper.readTree(response.getBody());

            return rootNode.path("token_type").asText() + " " + rootNode.path("access_token").asText();
        } catch (Exception e) {
            throw new KisOAuthApiException(e);
        }
    }

    public List<String> getAllWebSocketKey() {
        List<String> webSocketKeys = new ArrayList<>();

        for (Pair<String, String> authKey : getAuthKey())
            webSocketKeys.add(getWebSocketKey(authKey.getLeft(), authKey.getRight()));

        return webSocketKeys;
    }

    private List<Pair<String, String>> getAuthKey() {
        List<String> appKeys = kisProperties.getAppKey();
        List<String> appSecrets = kisProperties.getAppSecret();
        List<Pair<String, String>> authKeys = new ArrayList<>();

        for (int idx = 0; idx < Math.max(appKeys.size(), appSecrets.size()); idx++)
            authKeys.add(Pair.of(appKeys.get(idx), appSecrets.get(idx)));

        return authKeys;
    }

    /**
     * 한국투자증권 웹소켓 접속키 발급 메서드
     *
     * @param appKey    : AppKey
     * @param appSecret : AppSecret
     * @return          : 웹소켓 접속키
     */
    private String getWebSocketKey(String appKey, String appSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", appKey);
        body.put("secretkey", appSecret);

        HttpEntity<Map<String, String>> requestInfo = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    kisProperties.getUrl(KisUrlConstant.WEBSOCKET_KEY_ISSUE),
                    HttpMethod.POST,
                    requestInfo,
                    String.class
            );

            JsonNode rootNode = objectMapper.readTree(response.getBody());

            return rootNode.path("approval_key").asText();
        } catch (Exception e) {
            throw new KisOAuthApiException(e);
        }
    }
}