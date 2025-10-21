package muzusi.infrastructure.kis;

import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.kis.auth.KisAuthService;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KisRequestFactory {
    private final KisAuthService kisAuthService;
    private final KisProperties kisProperties;

    public HttpHeaders getHttpHeader(String trId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add("authorization", kisAuthService.getAccessToken());
        headers.add("appkey", kisProperties.getAppKeys().get(0));
        headers.add("appsecret", kisProperties.getAppSecrets().get(0));
        headers.add("tr_id", trId);
        headers.add("custtype", "P");

        return headers;
    }
    
    public HttpHeaders getHttpHeader(String trId, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        headers.add("authorization", accessToken);
        headers.add("appkey", kisProperties.getAppKeys().get(0));
        headers.add("appsecret", kisProperties.getAppSecrets().get(0));
        headers.add("tr_id", trId);
        headers.add("custtype", "P");
        
        return headers;
    }
}