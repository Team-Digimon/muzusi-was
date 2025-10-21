package muzusi.infrastructure.kis.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.trade.dto.TradeNotificationDto;
import muzusi.application.websocket.service.TradeNotificationPublisher;
import muzusi.domain.trade.type.TradeType;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisRealTimeTradeWebSocketHandler extends TextWebSocketHandler {
    private final TradeNotificationPublisher tradeNotificationPublisher;
    
    /**
     * 웹소켓 세션 연결 후 실행 메서드
     *
     * @param session       한국투자증권 웹소켓과 연결된 세션
     * @throws Exception    웹소켓 세션 연결 시 발생 예외
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("KIS Websocket session connected: {}", session.getId());
        super.afterConnectionEstablished(session);
    }
    
    /**
     * 웹소켓 세션 종료 후 실행 메서드
     *
     * @param session       한국투자증권 웹소켓과 연결되었던 세션
     * @param status        웹소켓 세션 연결 종료 상태
     * @throws Exception    웹소켓 세션 연결 종료 시 발생 예외
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("KIS Websocket session closed: {}", session.getId());
        super.afterConnectionClosed(session, status);
    }
    
    /**
     * 한국투자증권 웹소켓 세션을 통해 전달받은 메시지를 처리하는 메서드
     *
     * <p> 구독 주식 종목 실시간 체결가 메시지 수신 시, 해당 종목 구독자에게 메시지 송신
     *
     * <p> 세션 연결 유지를 위한 핑퐁(PingPong) 메시지 수신 시, 수신받은 페이로드와 동일한 페이로드를 응답
     *
     * @param session       웹소켓 세션
     * @param message       수신 메시지
     * @throws Exception    메시지 수신 시 발생 예외
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        
        if (isPingPong(payload)) {
            session.sendMessage(new TextMessage(payload));
            return;
        }
        
        if (isMetaMessage(payload)) {
            if (isErrorMessage(payload)) {
                log.error("[Error] Error message from KIS websocket - {}", payload);
            } else {
                log.info("[KIS Websocket] - {}", payload);
            }
            return;
        }
        
        tradeNotificationPublisher.publishTradeNotification(parsePayloadToNotificationDto(payload));
    }
    
    /**
     * 페이로드의 핑퐁 메시지 여부를 확인하는 메서드
     *
     * @param payload   페이로드
     * @return          페이로드의 핑퐁 메시지 여부
     */
    private boolean isPingPong(String payload) {
        return payload.contains("PINGPONG");
    }
    
    /**
     * 페이로드의 메타 메시지 여부를 확인하는 메서드
     *
     * @param payload   페이로드
     * @return          페이로드의 메타 메시지 여부
     */
    private boolean isMetaMessage(String payload) {
        return payload != null && !payload.isBlank() && payload.startsWith("{");
    }
    
    /**
     * 페이로드의 에러 메시지 여부를 확인하는 메서드
     *
     * <p> 웹소켓 응답 메타 메시지의 반환 코드(rt_cd)가 0을 제외한 나머지 경우는 모두 에러 응답
     *
     * @param payload   페이로드
     * @return          페이로드의 에러 메시지 여부
     */
    private boolean isErrorMessage(String payload) {
        return !payload.contains("\"rt_cd\": \"0\"");
    }
    
    /**
     * 페이로드를 해당 주식 종목 구독자에게 전달하기 위한 객체({@link TradeNotificationDto})로 변환하는 메서드
     *
     * @param payload   페이로드
     * @return          {@link TradeNotificationDto} 객체 리스트
     */
    private List<TradeNotificationDto> parsePayloadToNotificationDto(String payload) {
        List<TradeNotificationDto> result = new ArrayList<>();
        String[] parts = payload.split("\\^");
        String[] metas = parts[0].split("\\|");
        int tradeCount = Integer.parseInt(metas[2]);
        String stockCode = metas[3];
        
        for (int idx = 0, i = 0; i < tradeCount; i++) {
            result.add(TradeNotificationDto.builder()
                    .stockCode(stockCode)
                    .time(convertTime(parts[idx + 1]))
                    .price(Long.valueOf(parts[idx + 2]))
                    .stockCount(Long.valueOf(parts[idx + 12]))
                    .volume(Long.valueOf(parts[idx + 13]))
                    .tradeType((parts[idx + 21].equals("1")) ? TradeType.BUY : TradeType.SELL)
                    .changeRate(Double.valueOf(parts[idx + 5]))
                    .build());
            idx += 46;
        }
        
        return result;
    }
    
    /**
     * 페이로드에서 전달된 시각을 양식에 맞게 변환하는 메서드
     *
     * @param time  페이로드에 전달된 시각
     * @return      양식에 맞게 변환된 시각
     */
    private String convertTime(String time) {
        return time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);
    }
}
