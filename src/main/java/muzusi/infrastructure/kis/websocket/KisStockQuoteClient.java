package muzusi.infrastructure.kis.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisStockQuoteClient {
    private final ObjectMapper objectMapper;
    private static final String TRADE_ID = "H0STCNT0";
    
    private enum TradeType {
        SUBSCRIPTION("1"),
        UNSUBSCRIPTION("2");
        
        private final String value;
        
        TradeType(String value) {
            this.value = value;
        }
        
        public String getValue() {
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
        
        KisStockQuoteRequest request = KisStockQuoteRequest.of(webSocketKey, stockCode, tradeType);
        
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(request)));
        } catch (Exception e) {
            log.error("[Error] Failed to send request KIS Websocket - {} / {}", stockCode, e.getMessage());
        }
    }
    
    private record KisStockQuoteRequest(Header header, Body body) {
        private record Header(
                @JsonProperty("approval_key") String approvalKey,
                @JsonProperty("custtype") String custType,
                @JsonProperty("tr_type") String trType,
                @JsonProperty("content-type") String contentType
        ) { }
        
        private record Body(
                Input input
        ) {
            private record Input(
                    @JsonProperty("tr_id") String trId,
                    @JsonProperty("tr_key") String trKey
            ) { }
        }
        
        private static KisStockQuoteRequest of(String webSocketKey, String stockCode, TradeType type) {
            return new KisStockQuoteRequest(
                    new Header(webSocketKey, "P", type.getValue(), "utf-8"),
                    new Body(new Body.Input(TRADE_ID, stockCode))
            );
        }
    }
}
