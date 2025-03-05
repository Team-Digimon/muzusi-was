package muzusi.infrastructure.kis;

import lombok.RequiredArgsConstructor;
import muzusi.application.kis.dto.KisAuthDto;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KisAuthService {
    private final RedisService redisService;

    public KisAuthDto.AccessToken getAccessToken() {
        return (KisAuthDto.AccessToken) redisService.get(KisConstant.ACCESS_TOKEN_PREFIX.getValue());
    }

    public List<KisAuthDto.WebSocketKey> getWebSocketKey() {
        return redisService.getList(KisConstant.WEBSOCKET_KEY_PREFIX.getValue())
                    .stream().map(key -> (KisAuthDto.WebSocketKey) key).toList();
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
        redisService.setList(KisConstant.WEBSOCKET_KEY_PREFIX.getValue(), webSocketKey);
    }
}