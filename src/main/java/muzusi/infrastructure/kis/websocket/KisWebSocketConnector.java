package muzusi.infrastructure.kis.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.global.exception.KisApiException;
import muzusi.infrastructure.kis.dto.SessionConnectionDto;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisWebSocketConnector {
    private final KisWebSocketSessionManager kisWebSocketSessionManager;
    private final ObjectMapper objectMapper;

    @Getter
    @RequiredArgsConstructor
    public enum RequestType {
        SUBSCRIBE("1"), UNSUBSCRIBE("2");

        private final String value;
    }

    /**
     * 한국투자증권 웹소켓 특정 종목 구독 요청 메서드
     *
     * @param trId : 트랜잭션 아이디
     * @param stockCode : 주식 종목 코드
     */
    public void subscribe(String trId, String stockCode) {
        SessionConnectionDto session = kisWebSocketSessionManager.getSessionToSubscribe(trId, stockCode);

        if (session == null)
            throw new KisApiException(new RuntimeException("No available session"));

        sendMessage(session.getSession(), session.getWebSocketKey(), trId, stockCode, RequestType.SUBSCRIBE);
    }

    /**
     * 한국투자증권 웹소켓 특정 종목 구독 해제 요청 메서드
     * 
     * @param trId : 트랜잭션 아이디
     * @param stockCode : 주식 종목 코드
     */
    public void unsubscribe(String trId, String stockCode) {
        SessionConnectionDto session = kisWebSocketSessionManager.getSessionToUnsubscribe(trId, stockCode);

        if (session == null)
            throw new KisApiException(new RuntimeException("No available session"));

        sendMessage(session.getSession(), session.getWebSocketKey(), trId, stockCode, RequestType.UNSUBSCRIBE);
    }

    /**
     * 한국투자증권 웹소켓 메시지 전달 메서드
     *
     * @param session : 웹소켓 세션
     * @param webSocketKey : 세션 별 접속키
     * @param trId : 트랜잭션 아이디
     * @param stockCode : 주식 종목 코드
     * @param requestType : 구독/해제 여부
     */
    private void sendMessage(WebSocketSession session, String webSocketKey, String trId, String stockCode, RequestType requestType) {
        Map<String, String> header = new HashMap<>();
        header.put("approval_key", webSocketKey);
        header.put("custtype", "P");
        header.put("tr_type", requestType.getValue());
        header.put("tr_type", "1");
        header.put("content-type", "utf-8");

        Map<String, Object> body = new HashMap<>();
        Map<String, String> input = new HashMap<>();
        input.put("tr_id", trId);
        input.put("tr_key", stockCode);
        body.put("input", input);

        Map<String, Object> request = new HashMap<>();
        request.put("header", header);
        request.put("body", body);


        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(request)));
        } catch (Exception e) {
            throw new KisApiException(e);
        }
    }

    /**
     * 한국투자증권 웹소켓 세션 연결 유지 요청 메서드
     * 
     * @param session : 웹소켓 세션
     */
    public void keepConnection(WebSocketSession session) {
        if (session == null || !session.isOpen())
            throw new KisApiException(new RuntimeException("Session is closed"));

        Map<String, String> header = new HashMap<>();
        header.put("tr_id", "PINGPONG");
        header.put("datetime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        Map<String, Object> input = new HashMap<>();
        input.put("header", header);
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(input)));
        } catch (IOException e) {
            throw new KisApiException(e);
        }
    }
}