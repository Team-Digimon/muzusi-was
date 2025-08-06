package muzusi.application.kis.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.infrastructure.kis.KisAuthService;
import muzusi.infrastructure.kis.KisOAuthClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisAuthUpdater {
    private final KisOAuthClient kisOAuthClient;
    private final KisAuthService kisAuthService;
    
    /**
     * 서버 애플리케이션 구동 시 액세스 토큰 및 웹 소켓 접속키 발급 메서드
     */
    @PostConstruct
    public void saveKisAuthKey() {
        this.saveAccessToken();
        this.saveWebSocketKey();
    }

    /**
     * 한국투자증권 접근토큰 발급 API 호출 및 접속 토큰 저장 메서드
     * - 접근토큰 발급 오류 발생 시, DB 데이터 갱신 미실시
     */
    public void saveAccessToken() {
        String accessToken = kisOAuthClient.getAccessToken();

        if (accessToken != null) {
            kisAuthService.deleteAccessToken();
            kisAuthService.saveAccessToken(accessToken);
        }
    }

    /**
     * 한국투자증권 웹소켓 접속키 발급 API 호출 및 웹소켓 접속키 저장 메서드
     * - 웹소켓 접속키 발급 오류 발생 시, DB 데이터 갱신 미실시
     */
    public void saveWebSocketKey() {
        String webSocketKey = kisOAuthClient.getWebSocketKey();

        if (webSocketKey != null) {
            kisAuthService.deleteWebSocketKey();
            kisAuthService.saveWebSocketKey(webSocketKey);
        }
    }
}