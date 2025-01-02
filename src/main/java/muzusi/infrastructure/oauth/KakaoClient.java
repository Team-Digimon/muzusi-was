package muzusi.infrastructure.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import muzusi.application.auth.dto.UserInfoDto;
import muzusi.global.exception.CustomException;
import muzusi.global.response.error.type.CommonErrorType;
import muzusi.infrastructure.properties.KakaoProperties;
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
public class KakaoClient extends OAuthClient {
    private final KakaoProperties kakaoProperties;

    public KakaoClient(OAuthService oAuthService, KakaoProperties kakaoProperties) {
        super(oAuthService);
        this.kakaoProperties = kakaoProperties;
    }

    @Override
    protected String getTokenUri() {
        return kakaoProperties.getTokenUri();
    }

    @Override
    protected String getUserInfoUri() {
        return kakaoProperties.getUserInfoUri();
    }

    @Override
    protected MultiValueMap<String, String> getAccessTokenParams(String code) {
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