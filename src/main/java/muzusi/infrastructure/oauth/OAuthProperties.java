package muzusi.infrastructure.oauth;

import lombok.Builder;

@Builder
public record OAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String tokenUrl,
        String userInfoUrl
) { }
