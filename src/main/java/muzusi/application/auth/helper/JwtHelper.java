package muzusi.application.auth.helper;

import lombok.RequiredArgsConstructor;
import muzusi.application.auth.dto.TokenDto;
import muzusi.domain.user.entity.User;
import muzusi.global.security.jwt.JwtProvider;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JwtHelper {
    private final JwtProvider jwtProvider;

    public TokenDto createToken(User user) {
        Long userId = user.getId();
        String username = user.getUsername();

        String accessToken = jwtProvider.generateAccessToken(username, userId);
        String refreshToken = jwtProvider.generateRefreshToken(username, userId);

        return TokenDto.of(accessToken, refreshToken);
    }
}
