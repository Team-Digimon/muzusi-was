package muzusi.infrastructure.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import muzusi.application.auth.dto.UserInfoDto;
import muzusi.infrastructure.properties.OAuthProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class NaverClient extends OAuthClient {

    public NaverClient(OAuthService oAuthService, OAuthProperties oAuthProperties) {
        super(oAuthService, oAuthProperties.getNaver());
    }

    @Override
    protected MultiValueMap<String, String> getAccessTokenParams(OAuthProperties.Platform naverProperties, String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naverProperties.getClientId());
        params.add("client_secret", naverProperties.getClientSecret());
        params.add("redirect_uri", naverProperties.getRedirectUri());
        params.add("code", code);
        return params;
    }

    @Override
    protected UserInfoDto parseUserInfo(JsonNode rootNode) {
        String id = rootNode.path("response").get("id").asText();
        return UserInfoDto.of(id);
    }
}