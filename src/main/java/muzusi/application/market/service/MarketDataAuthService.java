package muzusi.application.market.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.market.port.MarketDataAuthPort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarketDataAuthService {
    private final MarketDataAuthPort marketDataAuthPort;
    
    /**
     * 주식 관련 정보 제공 외부 인프라 연동에 필요한 인증 자격증명을 발급하는 메서드
     *
     * <p> REST API 호출용 접근 토큰 발급
     * <p> 실시간 시세 구독용 웹소켓 접속키 발급
     */
    public void issueCredentials() {
        marketDataAuthPort.issueApiCredentials();
        marketDataAuthPort.issueWebSocketCredentials();
    }
}
