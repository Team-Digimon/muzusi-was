package muzusi.infrastructure.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth2.naver")
@Getter @Setter
public class NaverProperties {
    private String tokenUri;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String getUserInfoUri;
}
