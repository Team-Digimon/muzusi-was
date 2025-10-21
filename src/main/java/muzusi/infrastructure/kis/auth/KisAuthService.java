package muzusi.infrastructure.kis.auth;

import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KisAuthService {
    private final RedisService redisService;

    public String getAccessToken() {
        return (String) redisService.get(KisConstant.ACCESS_TOKEN_PREFIX.getValue());
    }

    public List<String> getWebSocketKeys() {
        return redisService.getList(KisConstant.WEBSOCKET_KEY_PREFIX.getValue()).stream()
                .map(obj -> (String) obj)
                .toList();
    }

    public void deleteAccessToken() {
        redisService.del(KisConstant.ACCESS_TOKEN_PREFIX.getValue());
    }

    public void saveAccessToken(String accessToken) {
        redisService.set(KisConstant.ACCESS_TOKEN_PREFIX.getValue(), accessToken, Duration.ofDays(1));
    }

    public void deleteWebSocketKey() {
        redisService.del(KisConstant.WEBSOCKET_KEY_PREFIX.getValue());
    }

    public void saveWebSocketKeys(List<String> webSocketKeys) {
        redisService.setList(KisConstant.WEBSOCKET_KEY_PREFIX.getValue(), webSocketKeys);
    }
}