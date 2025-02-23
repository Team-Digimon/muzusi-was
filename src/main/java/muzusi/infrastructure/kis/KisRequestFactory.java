package muzusi.infrastructure.kis;

import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KisRequestFactory {
    private final KisAuthProvider kisAuthProvider;
    private final KisProperties kisProperties;

    public HttpHeaders getHttpHeader(String trId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add("authorization", kisAuthProvider.getAccessToken().getValue());
        headers.add("appkey", kisProperties.getAppKey());
        headers.add("appsecret", kisProperties.getAppSecret());
        headers.add("tr_id", trId);
        headers.add("custtype", "P");

        return headers;
    }
}