package muzusi.application.auth.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.auth.dto.LoginDto;
import muzusi.application.auth.dto.TokenDto;
import muzusi.application.auth.dto.UserStatusDto;
import muzusi.application.auth.helper.JwtHelper;
import muzusi.application.auth.service.client.OAuthClient;
import muzusi.application.auth.service.client.OAuthClientFactory;
import muzusi.domain.user.type.OAuthPlatform;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final OAuthClientFactory oAuthClientFactory;
    private final JwtHelper jwtHelper;

    /**
     * 서비스 로그인을 위한 메서드
     *
     * @param platform : 로그인 플랫폼
     * @param code : 플랫폼의 인가 코드
     * @return : jwt token, 최초 가입 여부
     */
    public LoginDto signIn(OAuthPlatform platform, String code) {
        OAuthClient oAuthClient = oAuthClientFactory.getPlatformService(platform);

        UserStatusDto userStatusDto = oAuthClient.login(code);
        TokenDto tokenDto = jwtHelper.createToken(userStatusDto.user());

        return LoginDto.of(tokenDto, userStatusDto.isRegistered());
    }
}
