package muzusi.presentation.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import muzusi.application.auth.dto.LoginDto;
import muzusi.application.auth.dto.OAuthCodeDto;
import muzusi.application.auth.dto.TokenDto;
import muzusi.application.auth.service.AuthService;
import muzusi.domain.user.type.OAuthPlatform;
import muzusi.global.exception.CustomException;
import muzusi.global.response.error.type.CommonErrorType;
import muzusi.global.response.success.SuccessResponse;
import muzusi.global.util.cookie.CookieUtil;
import muzusi.global.util.jwt.AuthConstants;
import muzusi.presentation.auth.api.AuthApi;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
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

        return createTokenRes(authService.signIn(platform, oAuthCodeDto.code()));
    }

    @Override
    @GetMapping("/reissue")
    public ResponseEntity<?> reIssueToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null)
            throw new CustomException(CommonErrorType.REFRESH_TOKEN_NOT_FOUND);

        return createTokenRes(authService.reissueAccessToken(refreshToken));
    }

    @Override
    @GetMapping("/sign-out")
    public ResponseEntity<?> signOut(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                     HttpServletResponse response) {
        authService.signOut(refreshToken, response);

        return ResponseEntity.ok(SuccessResponse.ok());
    }

    private <T> ResponseEntity<?> createTokenRes(T dto) {
        Map<String, Object> responseData = new HashMap<>();

        TokenDto tokenDto = extractTokenDto(dto);
        responseData.put("accessToken", tokenDto.accessToken());

        if (dto instanceof LoginDto loginDto)
            responseData.put("isRegistered", loginDto.isRegistered());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        CookieUtil.createCookie(
                                AuthConstants.REFRESH_TOKEN_KEY.getValue(),
                                tokenDto.refreshToken(),
                                Duration.ofDays(7).toSeconds()
                        ).toString())
                .body(SuccessResponse.from(responseData));
    }

    private TokenDto extractTokenDto(Object dto) {
        if (dto instanceof LoginDto loginDto) {
            return loginDto.tokenDto();
        }
        if (dto instanceof TokenDto tokenDto) {
            return tokenDto;
        }
        throw new IllegalArgumentException("잘못된 형변환입니다. " + dto.getClass().getName());
    }

}
