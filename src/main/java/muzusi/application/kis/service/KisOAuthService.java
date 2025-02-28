package muzusi.application.kis.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.kis.dto.KisAuthDto;
import muzusi.infrastructure.kis.KisAuthService;
import muzusi.infrastructure.kis.KisOAuthClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisOAuthService {
    private final KisOAuthClient kisOAuthClient;
    private final KisAuthService kisAuthService;


    @PostConstruct
    public void saveKisAuthKey() {
        this.saveAccessToken();
        this.saveWebSocketKey();
    }

    /**
     * 한국투자증권 접근토큰 발급 API 호출 및 저장 메서드
     * 접근토큰 발급 오류 발생 시, DB 데이터 갱신 미실시
     */
    public void saveAccessToken() {
        String response = kisOAuthClient.getAccessToken();

        if (response != null) {
            KisAuthDto.AccessToken accessToken = KisAuthDto.AccessToken.builder()
                    .value(response)
                    .build();

            kisAuthService.deleteAccessToken();
            kisAuthService.saveAccessToken(accessToken);
        }
    }

    /**
     * 한국투자증권 웹소켓 접속키 발급 API 호출 및 저장 메서드
     * 웹소켓 접속키 발급 오류 발생 시, DB 데이터 갱신 미실시
     */
    public void saveWebSocketKey() {
        String response = kisOAuthClient.getWebSocketKey();

        if (response != null) {
            KisAuthDto.WebSocketKey webSocketKey = KisAuthDto.WebSocketKey.builder()
                    .value(kisOAuthClient.getWebSocketKey())
                    .build();

            kisAuthService.saveWebSocketKey(webSocketKey);
        }
    }
}