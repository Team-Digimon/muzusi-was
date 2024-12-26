package muzusi.application.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import muzusi.application.auth.dto.LoginDto;
import muzusi.application.auth.dto.TokenDto;
import muzusi.application.auth.dto.UserInfoDto;
import muzusi.application.auth.dto.UserStatusDto;
import muzusi.application.auth.helper.JwtHelper;
import muzusi.application.auth.service.client.OAuthClient;
import muzusi.application.auth.service.client.OAuthClientFactory;
import muzusi.domain.user.entity.User;
import muzusi.domain.user.service.UserService;
import muzusi.domain.user.type.OAuthPlatform;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final OAuthClientFactory oAuthClientFactory;
    private final JwtHelper jwtHelper;
    private final UserService userService;

    /**
     * 서비스 로그인을 위한 메서드
     *
     * @param platform : 로그인 플랫폼
     * @param code : 플랫폼의 인가 코드
     * @return : jwt token, 최초 가입 여부
     */
    @Transactional
    public LoginDto signIn(OAuthPlatform platform, String code) {
        OAuthClient oAuthClient = oAuthClientFactory.getPlatformService(platform);

        UserInfoDto userInfoDto = oAuthClient.fetchUserInfoFromPlatform(code);
        UserStatusDto userStatusDto = findOrRegisterUser(platform, userInfoDto.id());
        TokenDto tokenDto = jwtHelper.createToken(userStatusDto.user());

        return LoginDto.of(tokenDto, userStatusDto.isRegistered());
    }

    /**
     * 사용자 상태 확인 및 등록
     *
     * @param platform : OAuth 플랫폼
     * @param platformUserId : 플랫폼 사용자 ID
     * @return : 사용자 상태 정보
     */
    private UserStatusDto findOrRegisterUser(OAuthPlatform platform, String platformUserId) {
        String username = platform + "_" + platformUserId;

        return userService.readByUsername(username)
                .map(user -> UserStatusDto.of(user, true))
                .orElseGet(() -> registerNewUser(username, platform));
    }

    /**
     * 신규 사용자 등록
     *
     * @param username : 사용자 이름
     * @param platform : OAuth 플랫폼
     * @return : 등록된 사용자
     */
    private UserStatusDto registerNewUser(String username, OAuthPlatform platform) {
        User newUser = userService.save(
                User.builder()
                        .username(username)
                        .nickname(username)
                        .platform(platform)
                        .build()
        );
        return UserStatusDto.of(newUser, false);
    }

    /**
     * access token 재발급 메서드
     *
     * @param refreshToken : access token 재발급을 위한 refresh token
     * @return : jwt token
     */
    public TokenDto reissueAccessToken(String refreshToken) {
        return jwtHelper.reissueToken(refreshToken);
    }

    /**
     * 서비스 로그아웃 메서드
     *
     * @param refreshToken : refresh token
     * @param response : 쿠키 값 제거를 위한 response
     */
    public void signOut(String refreshToken, HttpServletResponse response) {
        jwtHelper.removeToken(refreshToken, response);
    }
}
