package muzusi.infrastructure.kis;

import lombok.RequiredArgsConstructor;
import muzusi.application.kis.dto.KisAuthDto;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class KisAuthService {
    private final RedisService redisService;
    private KisAuthDto.AccessToken accessToken;
    private KisAuthDto.WebSocketKey webSocketKey;

    public KisAuthDto.AccessToken getAccessToken() {
        if (this.accessToken == null)
            this.accessToken = (KisAuthDto.AccessToken) redisService.get(KisConstant.ACCESS_TOKEN_PREFIX.getValue());
        return this.accessToken;
    }

    public KisAuthDto.WebSocketKey getWebSocketKey() {
        if (this.webSocketKey == null)
            this.webSocketKey = (KisAuthDto.WebSocketKey) redisService.get(KisConstant.WEBSOCKET_KEY_PREFIX.getValue());
        return this.webSocketKey;
    }

    public void deleteAccessToken() {
        redisService.del(KisConstant.ACCESS_TOKEN_PREFIX.getValue());
    }

    public void saveAccessToken(KisAuthDto.AccessToken accessToken) {
        redisService.set(KisConstant.ACCESS_TOKEN_PREFIX.getValue(), accessToken, Duration.ofDays(1));
    }

    public void deleteWebSocketKey() {
        redisService.del(KisConstant.WEBSOCKET_KEY_PREFIX.getValue());
    }

    public void saveWebSocketKey(KisAuthDto.WebSocketKey webSocketKey) {
        redisService.set(KisConstant.WEBSOCKET_KEY_PREFIX.getValue(), webSocketKey, Duration.ofDays(1));
    }
}