package muzusi.application.kis.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.global.redis.RedisService;
import muzusi.infrastructure.kis.KisOAuthClient;
import muzusi.infrastructure.kis.KisConstant;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisOAuthService {
    private final KisOAuthClient kisOAuthClient;
    private final RedisService redisService;

    @PostConstruct
    public void issueWebSocketKey() {
        this.saveWebSocketKey();
    }

    /**
     * 한국투자증권 접근토큰 발급 API 호출 및 저장 메서드
     * 접근토큰 발급 오류 발생 시, DB 데이터 갱신 미실시
     */
    public void saveAccessToken() {
        String accessToken = kisOAuthClient.getAccessToken();

        if (accessToken != null)
            redisService.set(KisConstant.ACCESS_TOKEN_PREFIX.getValue(), accessToken, Duration.ofDays(1));
    }

    /**
     * 한국투자증권 웹소켓 접속키 발급 API 호출 및 저장 메서드
     * 웹소켓 접속키 발급 오류 발생 시, DB 데이터 갱신 미실시
     */
    public void saveWebSocketKey() {
        String webSocketKey = kisOAuthClient.getWebSocketKey();

        if (webSocketKey != null)
            redisService.set(KisConstant.WEBSOCKET_KEY_PREFIX.getValue(), webSocketKey, Duration.ofDays(365));
    }
}