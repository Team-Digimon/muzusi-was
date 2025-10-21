package muzusi.infrastructure.kis.websocket;

import lombok.extern.slf4j.Slf4j;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Slf4j
@Component
public class KisWebSocketConnector {
    private final WebSocketClient webSocketClient = new StandardWebSocketClient();
    private final KisRealTimeTradeWebSocketHandler kisRealTimeTradeWebSocketHandler;
    private final String webSocketDomain;
    
    public KisWebSocketConnector(
            KisProperties kisProperties,
            KisRealTimeTradeWebSocketHandler kisRealTimeTradeWebSocketHandler
    ) {
        this.webSocketDomain = kisProperties.getWebSocketDomain();
        this.kisRealTimeTradeWebSocketHandler = kisRealTimeTradeWebSocketHandler;
    }
    
    /**
     * 한국투자증권 웹소켓 세션 연결 메서드
     *
     * @return  한국투자증권 웹소켓과 연결된 세션
     */
    public WebSocketSession connect() {
        try {
            WebSocketSession session = webSocketClient
                    .execute(kisRealTimeTradeWebSocketHandler, webSocketDomain).join();
            
            return session;
        } catch (Exception e) {
            log.error("[Error] Failed to connect to KIS WebSocket - {}", e.getMessage());
            return null;
        }
    }
}
