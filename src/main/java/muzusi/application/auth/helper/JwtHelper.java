package muzusi.application.auth.helper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import muzusi.application.auth.dto.TokenDto;
import muzusi.application.auth.service.token.RefreshTokenService;
import muzusi.domain.user.entity.User;
import muzusi.global.exception.CustomException;
import muzusi.global.response.error.type.CommonErrorType;
import muzusi.global.security.jwt.JwtProvider;
import muzusi.global.util.cookie.CookieUtil;
import muzusi.global.util.jwt.AuthConstants;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtHelper {
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public TokenDto createToken(User user) {
        Long userId = user.getId();
        String username = user.getUsername();

        String accessToken = jwtProvider.generateAccessToken(username, userId);
        String refreshToken = jwtProvider.generateRefreshToken(username, userId);

        refreshTokenService.saveRefreshToken(username, refreshToken);

        return TokenDto.of(accessToken, refreshToken);
    }

    public TokenDto reissueToken(String refreshToken) {
        String username = jwtProvider.getUsername(refreshToken);

        if (!refreshTokenService.existedRefreshToken(username))
            throw new CustomException(CommonErrorType.REFRESH_TOKEN_NOT_FOUND);

        Long userId = jwtProvider.getUserId(refreshToken);

        String newAccessToken = jwtProvider.generateAccessToken(username, userId);
        String newRefreshToken = jwtProvider.generateRefreshToken(username, userId);

        refreshTokenService.saveRefreshToken(username, newRefreshToken);

        return TokenDto.of(newAccessToken, newRefreshToken);
    }

    public void removeToken(String refreshToken, HttpServletResponse response) {
        deleteRefreshToken(refreshToken, response);
    }

    private void deleteRefreshToken(String refreshToken, HttpServletResponse response) {
        String username = jwtProvider.getUsername(refreshToken);
        CookieUtil.deleteCookie(AuthConstants.REFRESH_TOKEN_KEY.getValue(), response);
        refreshTokenService.deleteRefreshToken(username);
    }
}
