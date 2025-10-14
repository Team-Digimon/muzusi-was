package muzusi.infrastructure.kis.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisRealTimeTradeWebSocketClient {
    private final ObjectMapper objectMapper;
    private static final String TRADE_ID = "H0STCNT0";
    
    private enum TradeType {
        SUBSCRIPTION(1),
        UNSUBSCRIPTION(2);
        
        private final int value;
        
        TradeType(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }
    
    /**
     * 한국투자증권 국내주식 실시간 체결가 웹소켓 구독 요청 메서드
     *
     * @param session       웹소켓 세션
     * @param webSocketKey  웹소켓 접속키
     * @param stockCode     주식 종목 코드
     */
    public void subscribe(WebSocketSession session, String webSocketKey, String stockCode) {
        this.request(session, webSocketKey, stockCode, TradeType.SUBSCRIPTION);
    }
    
    /**
     * 한국투자증권 국내주식 실시간 체결가 웹소켓 구독 해제 요청 메서드
     *
     * @param session       웹소켓 세션
     * @param webSocketKey  웹소켓 접속키
     * @param stockCode     주식 종목 코드
     */
    public void unsubscribe(WebSocketSession session, String webSocketKey, String stockCode) {
        this.request(session, webSocketKey, stockCode, TradeType.UNSUBSCRIPTION);
    }
    
    /**
     * 한국투자증권 국내주식 실시간 체결가 웹소켓 요청 메서드
     *
     * @param session       웹소켓 세션
     * @param webSocketKey  웹소켓 접속키
     * @param stockCode     주식 종목 코드
     * @param tradeType     거래 타입 (1: 구독, 2: 해제)
     */
    private void request(WebSocketSession session, String webSocketKey, String stockCode, TradeType tradeType) {
        if (session == null || !session.isOpen()) {
            log.error("[Error] Failed to send request KIS Websocket - Session is null or closed.");
            return;
        }
        
        Map<String, String> header = new HashMap<>();
        header.put("approval_key", webSocketKey);
        header.put("custtype", "P");
        header.put("tr_type", String.valueOf(tradeType.getValue()));
        header.put("content-type", "utf-8");
        
        Map<String, Object> body = new HashMap<>();
        Map<String, String> input = new HashMap<>();
        input.put("tr_id", TRADE_ID);
        input.put("tr_key", stockCode);
        body.put("input", input);
        
        Map<String, Object> request = new HashMap<>();
        request.put("header", header);
        request.put("body", body);
        
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(request)));
        } catch (Exception e) {
            log.error("[Error] Failed to send request KIS Websocket - {} / {}", stockCode, e.getMessage());
        }
    }
}
