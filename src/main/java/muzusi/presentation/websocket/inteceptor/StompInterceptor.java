package muzusi.presentation.websocket.inteceptor;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.service.StockSearchService;
import muzusi.infrastructure.kis.websocket.KisWebSocketConnector;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {
    private final KisWebSocketConnector kisWebSocketConnector;
    private final StockSearchService stockSearchService;

    private final static String STOCK_CODE_HEADER_NAME = "stockCode";
    private final static String TR_ID_HEADER_NAME = "trId";

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
        Pair<String, String> headers = extractCustomHeader(accessor);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            stockSearchService.increaseStockSearchCount(headers.getRight());
            kisWebSocketConnector.subscribe(headers.getLeft(), headers.getRight());
        }

        if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand()))
            kisWebSocketConnector.unsubscribe(headers.getLeft(), headers.getRight());

        return message;
    }

    /**
     *
     * @param accessor : Stomp 메시지 헤더 접근자
     * @return : 커스텀 헤더명 (trId, stockCode)
     */
    private Pair<String, String> extractCustomHeader(StompHeaderAccessor accessor) {
        return Pair.of(
                accessor.getFirstNativeHeader(TR_ID_HEADER_NAME),
                accessor.getFirstNativeHeader(STOCK_CODE_HEADER_NAME)
        );
    }
}