package muzusi.application.kis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

    public void keepConnection() {
        Map<String, String> header = new HashMap<>();
        header.put("tr_id", "PINGPONG");
        header.put("datetime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        Map<String, Object> input = new HashMap<>();
        input.put("header", header);
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(input)));
        } catch (IOException e) {
            log.error("[ERROR] {}", e.getMessage());
        }
    }

    public abstract void connect(String stockCode);
    public abstract void disconnect(String stockCode);
}