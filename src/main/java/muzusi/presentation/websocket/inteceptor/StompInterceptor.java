package muzusi.presentation.websocket.inteceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.stock.service.StockSearchService;
import muzusi.application.websocket.service.KisSubscriptionManager;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {
    private final KisSubscriptionManager kisSubscriptionManager;
    private final StockSearchService stockSearchService;

    private final static String STOCK_CODE_HEADER_NAME = "stockCode";

    /**
     * 특정 종목 구독 및 해제 시 한국투자증권 웹소켓 연결 관리를 위한 메서드
     *
     * - 구독 등록 시, 한국투자증권 주식 체결가 웹 소켓 등록 요청
     * - 구독 해제 시, 한국투자증권 주식 체결가 웹 소켓 해제 요청
     *
     * @param message : 수신 메시지
     * @param channel : 메시지 채널
     * @return        : 기본 처리 메서드 호출
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String stockCode = extractStockCode(accessor);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            try {
                stockSearchService.increaseStockSearchCount(stockCode);
                kisSubscriptionManager.subscribe(stockCode);
            } catch (Exception e) {
                log.error("[ERROR] Failed to subscribe stock {} - {}", stockCode, e.getMessage());
                return null;
            }
            
        }
        
        if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
            try {
                kisSubscriptionManager.unsubscribe(stockCode);
            } catch (Exception e) {
                log.error("[ERROR] Failed to unsubscribe stock {} - {}", stockCode, e.getMessage());
                return null;
            }
        }

        return message;
    }
    
    /**
     * STOMP 요청 메시지 헤더 내 주식 종목 코드 추출 메서드
     *
     * @param accessor  STOMP 헤더 접근 객체
     * @return          주식 종목 코드
     */
    private String extractStockCode(StompHeaderAccessor accessor) {
        return accessor.getFirstNativeHeader(STOCK_CODE_HEADER_NAME);
    }
}