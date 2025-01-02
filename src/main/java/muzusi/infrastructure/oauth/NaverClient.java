package muzusi.infrastructure.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import muzusi.application.auth.dto.UserInfoDto;
import muzusi.global.exception.CustomException;
import muzusi.global.response.error.type.CommonErrorType;
import muzusi.infrastructure.properties.NaverProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class NaverClient extends OAuthClient {
    private final NaverProperties naverProperties;

    public NaverClient(OAuthService oAuthService, NaverProperties naverProperties) {
        super(oAuthService);
        this.naverProperties = naverProperties;
    }

    @Override
    protected String getTokenUri() {
        return naverProperties.getTokenUri();
    }

    @Override
    protected String getUserInfoUri() {
        return naverProperties.getUserInfoUri();
    }

    @Override
    protected MultiValueMap<String, String> getAccessTokenParams(String code) {
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