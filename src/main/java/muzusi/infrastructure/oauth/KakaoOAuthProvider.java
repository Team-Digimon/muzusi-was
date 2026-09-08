package muzusi.infrastructure.oauth;

import muzusi.infrastructure.oauth.dto.AccessTokenResponse;
import muzusi.infrastructure.oauth.dto.KakaoAccessTokenResponse;
import muzusi.infrastructure.oauth.dto.KakaoUserInfoResponse;
import muzusi.infrastructure.oauth.dto.UserInfoResponse;
import muzusi.infrastructure.properties.KakaoDevelopersProperties;
import org.springframework.stereotype.Component;

@Component
public class KakaoOAuthProvider extends OAuthProvider {
    public KakaoOAuthProvider(OAuthClient oAuthClient, KakaoDevelopersProperties properties) {
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
        return KakaoAccessTokenResponse.class;
    }
    
    @Override
    protected Class<? extends UserInfoResponse> getUserInfoResponseType() {
        return KakaoUserInfoResponse.class;
    }
}