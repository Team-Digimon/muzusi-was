package muzusi.infrastructure.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import muzusi.application.auth.dto.UserInfoDto;
import muzusi.infrastructure.properties.OAuthProperties;
import org.springframework.util.MultiValueMap;

@RequiredArgsConstructor
public abstract class OAuthClient {
    private final OAuthService oAuthService;
    private final OAuthProperties.Platform oAuthProperties;

    /**
     * 플랫폼의 access token을 가져오기 위한 파라미터 생성 메서드.
     *
     * @param code : 플랫폼 인증 코드
     * @return : 파라미터 값
     */
    protected abstract MultiValueMap<String, String> getAccessTokenParams(
            OAuthProperties.Platform oAuthProperties,
            String code
    );

    /**
     * 플랫폼의 사용자 정보을 추출하는 메서드.
     *
     * @param rootNode : 플랫폼의 사용자 정보 json
     * @return : 추출한 사용자 정보
     */
    protected abstract UserInfoDto parseUserInfo(JsonNode rootNode);

    public UserInfoDto fetchUserInfoFromPlatform(String code) {
        String accessToken =
                oAuthService.getAccessToken(getTokenUri(), getAccessTokenParams(oAuthProperties, code));

        JsonNode userInfoNode = oAuthService.getUserInfo(getUserInfoUri(), accessToken);
        return parseUserInfo(userInfoNode);
    }

    /**
     * 플랫폼의 access token을 받아오기 위한 uri를 가져오는 메서드.
     *
     * @return : token uri
     */
    private String getTokenUri() {
        return oAuthProperties.getTokenUri();
    }

    /**
     * 플랫폼의 사용자 정보를 가져오기 위해 uri를 가져오는 메서드.
     *
     * @return : userInfo uri
     */
    private String getUserInfoUri() {
        return oAuthProperties.getUserInfoUri();
    }
}