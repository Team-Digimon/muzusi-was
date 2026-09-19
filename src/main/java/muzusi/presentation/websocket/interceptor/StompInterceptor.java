package muzusi.presentation.websocket.interceptor;

import lombok.extern.slf4j.Slf4j;
import muzusi.application.stockquote.exception.StockQuoteException;
import muzusi.application.stockquote.service.StockQuoteSubscriptionService;
import muzusi.application.stocksearch.service.StockSearchService;
import muzusi.global.response.error.ErrorResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StompInterceptor implements ChannelInterceptor {
    private final StockQuoteSubscriptionService stockQuoteSubscriptionService;
    private final StockSearchService stockSearchService;

    public StompInterceptor(
            StockQuoteSubscriptionService stockQuoteSubscriptionService,
            StockSearchService stockSearchService,
            @Lazy SimpMessagingTemplate messagingTemplate
    ) {
        this.stockQuoteSubscriptionService = stockQuoteSubscriptionService;
        this.stockSearchService = stockSearchService;
        this.messagingTemplate = messagingTemplate;
    }
    
    private final SimpMessagingTemplate messagingTemplate;

    private static final String STOCK_CODE_HEADER_NAME = "stockCode";
    private static final String ERROR_DESTINATION = "/queue/errors";

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
        String sessionId = accessor.getSessionId();
        String stockCode = extractStockCode(accessor);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            try {
                stockQuoteSubscriptionService.subscribe(stockCode);
            } catch (Exception e) {
                if (e instanceof StockQuoteException ex) {
                    log.warn("[Error/StockQuote] 주식 실시간 체결가 구독 실패 (stockCode: {}, message: {})", stockCode, ex.getMessage());
                }
                sendError(sessionId, e);
                return null;
            }

            increaseSearchCount(stockCode);
        }
        
        if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
            try {
                stockQuoteSubscriptionService.unsubscribe(stockCode);
            } catch (Exception e) {
                if (e instanceof StockQuoteException ex) {
                    log.warn("[Error/StockQuote] 주식 실시간 체결가 구독 해제 실패 (stockCode: {}, message: {})", stockCode, e.getMessage());
                }
                sendError(sessionId, e);
                return null;
            }
        }

        return message;
    }
    
    /**
     * 주식 종목의 검색 빈도를 증가시키는 메서드
     *
     * <p> 검색 빈도 증가에 실패하더라도 구독 흐름에 영향을 주지 않도록 예외를 로깅만 하고 무시한다.
     *
     * @param stockCode 주식 종목 코드
     */
    private void increaseSearchCount(String stockCode) {
        try {
            stockSearchService.increaseSearchCount(stockCode);
        } catch (Exception e) {
            log.warn("[Error/StockSearch] 주식 검색 빈도 증가 실패 (stockCode: {}, message: {})", stockCode, e.getMessage());
        }
    }

    /**
     * 특정 세션을 통해 에러 메시지를 전달하는 메서드
     *
     * @param sessionId 에러 메시지를 전달한 세션 ID
     * @param exception 예외 객체
     */
    private void sendError(String sessionId, Exception exception) {
        if (!(exception instanceof StockQuoteException e)) return;
        
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        
        messagingTemplate.convertAndSendToUser(
                sessionId,
                ERROR_DESTINATION,
                ErrorResponse.from(e.getErrorType(), e.getMessage()),
                headerAccessor.getMessageHeaders()
        );
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