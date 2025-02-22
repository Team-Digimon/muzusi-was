package muzusi.infrastructure.kis;

import lombok.RequiredArgsConstructor;
import muzusi.application.kis.dto.KisAuthDto;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KisAuthProvider {
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
}