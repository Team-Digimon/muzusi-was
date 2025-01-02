package muzusi.infrastructure.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import muzusi.application.auth.dto.UserInfoDto;
import muzusi.infrastructure.properties.OAuthProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class KakaoClient extends OAuthClient {

    public KakaoClient(OAuthService oAuthService, OAuthProperties oAuthProperties) {
        super(oAuthService, oAuthProperties.getKakao());
    }

    @Override
    protected MultiValueMap<String, String> getAccessTokenParams(OAuthProperties.Platform kakaoProperties, String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoProperties.getClientId());
        params.add("client_secret", kakaoProperties.getClientSecret());
        params.add("redirect_uri", kakaoProperties.getRedirectUri());
        params.add("code", code);
        return params;
    }

    @Override
    protected UserInfoDto parseUserInfo(JsonNode rootNode) {
        String id = rootNode.path("id").asText();
        return UserInfoDto.of(id);
    }
}