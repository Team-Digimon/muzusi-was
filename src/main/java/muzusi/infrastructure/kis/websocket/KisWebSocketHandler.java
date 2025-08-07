package muzusi.infrastructure.kis.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class KisWebSocketHandler extends TextWebSocketHandler {
    protected static WebSocketSession session;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;
        log.warn("KIS session connected: {}", session.getId());
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.warn("KIS session closed");
        super.afterConnectionClosed(session, status);
    }

    /**
     * 웹 소켓 연결 지속을 위한 메서드
     *
     * - 연결 유지를 위한 PINGPONG 메시지 송신
     */
    public void keepConnection() {
        if (session == null || !session.isOpen()) {
            log.error("KIS session closed in keep connection");
            return;
        }

        Map<String, String> header = new HashMap<>();
        header.put("tr_id", "PINGPONG");
        header.put("datetime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        Map<String, Object> input = new HashMap<>();
        input.put("header", header);
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(input)));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public abstract void connect(String stockCode);
    public abstract void disconnect(String stockCode);
}