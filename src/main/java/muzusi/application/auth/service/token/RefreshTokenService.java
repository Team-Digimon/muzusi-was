package muzusi.application.auth.service.token;

import lombok.RequiredArgsConstructor;
import muzusi.global.redis.RedisService;
import muzusi.global.util.jwt.AuthConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisService redisService;

    @Value("${token.refresh.in-redis}")
    private long REDIS_REFRESH_EXPIRATION;

    public void saveRefreshToken(String username, String refreshToken) {
        String key = AuthConstants.REFRESH_TOKEN_KEY.getValue() + ":" + username;
        redisService.set(key, refreshToken, Duration.ofSeconds(REDIS_REFRESH_EXPIRATION));
    }

    public boolean existedRefreshToken(String username) {
        String key = AuthConstants.REFRESH_TOKEN_KEY.getValue() + ":" + username;

        return redisService.existed(key);
    }

    public void deleteRefreshToken(String username) {
        String key = AuthConstants.REFRESH_TOKEN_KEY.getValue() + ":" + username;
        redisService.del(key);
    }
}
