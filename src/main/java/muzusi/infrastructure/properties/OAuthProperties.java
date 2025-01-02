package muzusi.infrastructure.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth2")
@Getter @Setter
public class OAuthProperties {
    private Platform kakao;
    private Platform naver;

    @Getter
    @RequiredArgsConstructor
    public static class Platform {
        private final String tokenUri;
        private final String userInfoUri;
        private final String clientId;
        private final String clientSecret;
        private final String redirectUri;
    }
}