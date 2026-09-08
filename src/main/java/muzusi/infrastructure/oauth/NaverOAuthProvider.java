package muzusi.infrastructure.oauth;

import muzusi.infrastructure.oauth.dto.AccessTokenResponse;
import muzusi.infrastructure.oauth.dto.NaverAccessTokenResponse;
import muzusi.infrastructure.oauth.dto.NaverUserInfoResponse;
import muzusi.infrastructure.oauth.dto.UserInfoResponse;
import muzusi.infrastructure.properties.NaverDevelopersProperties;
import org.springframework.stereotype.Component;

@Component
public class NaverOAuthProvider extends OAuthProvider {
    public NaverOAuthProvider(OAuthClient oAuthClient, NaverDevelopersProperties properties) {
        super(
                oAuthClient,
                OAuthProperties.builder()
                        .clientId(properties.getClientId())
                        .clientSecret(properties.getClientSecret())
                        .redirectUri(properties.getLogin().redirectUri())
                        .tokenUrl(properties.getLogin().tokenUrl())
                        .userInfoUrl(properties.getLogin().userInfoUrl())
                        .build()
        );
    }
    
    @Override
    protected Class<? extends AccessTokenResponse> getAccessTokenResponseType() {
        return NaverAccessTokenResponse.class;
    }
    
    @Override
    protected Class<? extends UserInfoResponse> getUserInfoResponseType() {
        return NaverUserInfoResponse.class;
    }
}