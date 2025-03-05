package muzusi.infrastructure.kis.websocket;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.global.exception.KisApiException;
import muzusi.infrastructure.kis.constant.KisWebSocketTransactionType;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisWebSocketRouter extends TextWebSocketHandler {
    private final RealTimeTradePublisher realTimeTradePublisher;
    private final KisWebSocketSessionManager kisWebSocketSessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        kisWebSocketSessionManager.addSession(session);
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        if (!payload.startsWith("{") && !payload.contains("PINGPONG")) {
            String[] parts = payload.split("\\^");
            String[] metas = parts[0].split("\\|");

            switch (KisWebSocketTransactionType.of(metas[1])) {
                case H0STCNT0 -> realTimeTradePublisher.execute(payload);
                default -> throw new KisApiException(new RuntimeException("No handler class for Response"));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        kisWebSocketSessionManager.deleteSession(session);
        super.afterConnectionClosed(session, status);
    }
}