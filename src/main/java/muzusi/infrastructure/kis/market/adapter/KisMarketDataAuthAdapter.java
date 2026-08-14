package muzusi.infrastructure.kis.market.adapter;

import lombok.RequiredArgsConstructor;
import muzusi.application.market.port.MarketDataAuthPort;
import muzusi.infrastructure.kis.auth.KisAuthStore;
import muzusi.infrastructure.kis.auth.KisOAuthClient;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KisMarketDataAuthAdapter implements MarketDataAuthPort {
    private final KisProperties kisProperties;
    private final KisOAuthClient kisOAuthClient;
    private final KisAuthStore kisAuthStore;
    
    /**
     * 한국투자증권 REST API 호출용 접근 토큰을 발급받아 저장하는 메서드
     *
     * <p> 기존 저장된 접근 토큰을 삭제한 후 새로 발급받은 토큰으로 갱신한다.
     */
    @Override
    public void issueApiCredentials() {
        String appKey = kisProperties.getAppKey();
        String appSecret = kisProperties.getAppSecret();
        String accessToken = kisOAuthClient.getAccessToken(appKey, appSecret);

        kisAuthStore.deleteAccessToken();
        kisAuthStore.saveAccessToken(accessToken);
    }

    /**
     * 한국투자증권 웹소켓 접속키를 발급받아 저장하는 메서드
     *
     * <p> 기존 저장된 웹소켓 접속키 목록을 삭제한 후 새로 발급받은 키 목록으로 갱신한다.
     * <p> 앱키, 앱시크릿 갯수만큼 웹소켓 세션을 유지하기 위하면 모든 앱키, 앱시크릿를 대상으로 웹소켓 접속키 발급한다.
     */
    @Override
    public void issueWebSocketCredentials() {
        List<String> appKeys = kisProperties.getAppKeys();
        List<String> appSecrets = kisProperties.getAppSecrets();
        List<String> webSocketKeys = new ArrayList<>();
        
        for (int i = 0; i < Math.min(appKeys.size(), appSecrets.size()); i++) {
            String appKey = appKeys.get(i);
            String appSecret = appSecrets.get(i);
            webSocketKeys.add(kisOAuthClient.getWebSocketKey(appKey, appSecret));
        }
        
        kisAuthStore.deleteWebSocketKey();
        kisAuthStore.saveWebSocketKeys(webSocketKeys);
    }
}
