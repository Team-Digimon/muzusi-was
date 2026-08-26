package muzusi.infrastructure.stockquote.adapter.kis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.stockquote.port.StockQuotePort;
import muzusi.infrastructure.kis.auth.KisAuthStore;
import muzusi.infrastructure.stockquote.requester.kis.KisStockQuoteRequester;
import muzusi.infrastructure.kis.websocket.KisWebSocketConnector;
import muzusi.infrastructure.kis.websocket.KisWebSocketSessionStore;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisStockQuoteAdapter implements StockQuotePort {
    private final KisWebSocketConnector kisWebSocketConnector;
    private final KisAuthStore kisAuthStore;
    private final KisWebSocketSessionStore kisWebSocketSessionStore;
    private final KisStockQuoteRequester kisStockQuoteRequester;
    
    /**
     * 한국투자증권 웹소켓 세션을 연결하는 메서드
     *
     * <p> 발급받은 웹소켓 접속키 목록으로 각각 세션을 연결하고, 연결된 세션을 저장소({@link KisWebSocketSessionStore})에 저장
     *
     * @return 연결된 웹소켓 세션 ID 목록
     */
    @Override
    public List<String> connect() {
        List<String> webSocketKeys = kisAuthStore.getWebSocketKeys();
        List<String> connectedSessionIds = new ArrayList<>();
        
        for (String webSocketKey : webSocketKeys) {
            try {
                WebSocketSession session = kisWebSocketConnector.connect();
                String connectedSessionId = kisWebSocketSessionStore.save(session, webSocketKey);
                connectedSessionIds.add(connectedSessionId);
            } catch (Exception e) {
                log.error("[Error] Failed to connect to KIS Websocket - {}", e.getMessage(), e);
            }
        }
        
        return connectedSessionIds;
    }
    
    /**
     * 한국투자증권 웹소켓 세션을 모두 종료하고, 저장소({@link KisWebSocketSessionStore})에서 삭제하는 메서드
     */
    @Override
    public void disconnect() {
        kisWebSocketSessionStore.deleteAll();
    }

    /**
     * 특정 웹소켓 세션을 통해 주식 종목의 실시간 체결가 구독을 요청하는 메서드
     *
     * @param sessionId 구독을 요청할 웹소켓 세션 ID
     * @param stockCode 구독할 주식 종목 코드
     */
    @Override
    public void subscribe(String sessionId, String stockCode) {
        KisWebSocketSessionStore.KisWebSocketSession kisWebSocketSession = kisWebSocketSessionStore.findBySessionId(sessionId);

        if (kisWebSocketSession == null) {
            log.error("[Error] Can not find WebSocketSession to subscribe - {}", sessionId);
            return;
        }

        String webSocketKey = kisWebSocketSession.getWebSocketKey();
        WebSocketSession session = kisWebSocketSession.getWebSocketSession();

        kisStockQuoteRequester.subscribe(session, webSocketKey, stockCode);
    }

    /**
     * 특정 웹소켓 세션을 통해 주식 종목의 실시간 체결가 구독 해제를 요청하는 메서드
     *
     * @param sessionId 구독 해제를 요청할 웹소켓 세션 ID
     * @param stockCode 구독 해제할 주식 종목 코드
     */
    @Override
    public void unsubscribe(String sessionId, String stockCode) {
        KisWebSocketSessionStore.KisWebSocketSession kisWebSocketSession = kisWebSocketSessionStore.findBySessionId(sessionId);
        
        if (kisWebSocketSession == null) {
            log.error("[Error] Can not find WebSocketSession to unsubscribe - {}", sessionId);
            return;
        }
        
        String webSocketKey = kisWebSocketSession.getWebSocketKey();
        WebSocketSession session = kisWebSocketSession.getWebSocketSession();
        
        kisStockQuoteRequester.unsubscribe(session, webSocketKey, stockCode);
    }
}
