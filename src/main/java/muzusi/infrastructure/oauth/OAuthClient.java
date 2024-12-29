package muzusi.infrastructure.oauth;

import muzusi.application.auth.dto.UserInfoDto;

public abstract class OAuthClient {

    /**
     * code를 통해 OAuth 플랫폼의 AccessToken 받아오는 메서드
     *
     * @param code : 플랫폼 인증 코드
     * @return : 플랫폼의 accessToken
     */
    public abstract String getAccessToken(String code);

    /**
     * 플랫폼의 AccessToken을 통해 사용자 정보 불러오는 메서드
     *
     * @param accessToken : 플랫폼의 AccessToken
     * @return : 플랫폼 사용자 정보
     */
    public abstract UserInfoDto getUserInfo(String accessToken);

    public UserInfoDto fetchUserInfoFromPlatform(String code) {
        String accessToken = getAccessToken(code);
        return getUserInfo(accessToken);
    }
}
