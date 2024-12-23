package muzusi.presentation.auth.controller;

import lombok.RequiredArgsConstructor;
import muzusi.application.auth.dto.LoginDto;
import muzusi.application.auth.dto.OAuthCodeDto;
import muzusi.application.auth.service.AuthService;
import muzusi.domain.user.type.OAuthPlatform;
import muzusi.global.response.success.SuccessResponse;
import muzusi.global.util.cookie.CookieUtil;
import muzusi.global.util.jwt.AuthConstants;
import muzusi.presentation.auth.api.AuthApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {
    private final AuthService authService;

    @Override
    @PostMapping("/sign-in/{platform}")
    public ResponseEntity<?> signIn(@RequestBody OAuthCodeDto oAuthCodeDto,
                                    @PathVariable OAuthPlatform platform) {
        LoginDto loginDto = authService.signIn(platform, oAuthCodeDto.code());

        return createTokenRes(loginDto);
    }

    private ResponseEntity<?> createTokenRes(LoginDto loginDto) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("accessToken", loginDto.tokenDto().accessToken());
        responseData.put("isRegistered", loginDto.isRegistered());

        return ResponseEntity.ok()
                .header("Set-Cookie",
                        CookieUtil.createCookie(
                                AuthConstants.REFRESH_TOKEN_KEY.getValue(),
                                loginDto.tokenDto().refreshToken(),
                                Duration.ofDays(7).toSeconds()
                        ).toString())
                .body(SuccessResponse.from(responseData));
    }
}
