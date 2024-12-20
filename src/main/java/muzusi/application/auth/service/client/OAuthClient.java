package muzusi.application.auth.service.client;

import muzusi.application.auth.dto.UserStatusDto;
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

    /**
     * 플랫폼 id를 통해 로컬 서버 사용자 정보 불러오는 메서드
     *
     * @param platformId : 플랫폼의 id
     * @return : 사용자 정보
     */
    public abstract UserStatusDto getOAuthUser(String platformId);

    public UserStatusDto login(String code) {
        String accessToken = getAccessToken(code);
        UserInfoDto userInfoDto = getUserInfo(accessToken);

        return getOAuthUser(userInfoDto.id());
    }
}
