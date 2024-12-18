package muzusi.application.auth.service;

import muzusi.application.auth.dto.UserInfoDto;

public abstract class OAuthClient {

    public abstract String getAccessToken(String code);

    public abstract UserInfoDto getUserInfo(String accessToken);

    public UserInfoDto login(String code) {
        String accessToken = getAccessToken(code);
        return getUserInfo(accessToken);
    }
}
