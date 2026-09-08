package muzusi.infrastructure.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter @Setter
@ConfigurationProperties(prefix = "naver.developers")
public class NaverDevelopersProperties {
    private String clientId;
    private String clientSecret;
    private Login login;

    public record Login(
            String redirectUri,
            String tokenUrl,
            String userInfoUrl
    ) { }
}
