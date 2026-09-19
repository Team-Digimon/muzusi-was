package muzusi.presentation.websocket.interceptor;

import muzusi.application.stockquote.exception.StockQuoteException;
import muzusi.application.stockquote.service.StockQuoteSubscriptionService;
import muzusi.application.stocksearch.service.StockSearchService;
import muzusi.domain.stockquote.exception.StockQuoteErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StompInterceptorTest {

    private static final String STOCK_CODE = "005930";

    @Mock
    private StockQuoteSubscriptionService stockQuoteSubscriptionService;

    @Mock
    private StockSearchService stockSearchService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private MessageChannel channel;

    private StompInterceptor stompInterceptor;

    @BeforeEach
    void setUp() {
        stompInterceptor = new StompInterceptor(stockQuoteSubscriptionService, stockSearchService, messagingTemplate);
    }

    @Test
    @DisplayName("구독에 성공하면 검색 빈도를 증가시키고 메시지를 그대로 통과시킨다")
    void preSend_subscribeSuccess_increasesSearchCount() {
        // given
        Message<?> message = createMessage(StompCommand.SUBSCRIBE);

        // when
        Message<?> result = stompInterceptor.preSend(message, channel);

        // then
        assertThat(result).isSameAs(message);
        verify(stockSearchService).increaseSearchCount(STOCK_CODE);
    }

    @Test
    @DisplayName("구독에 실패하면 검색 빈도를 증가시키지 않고 메시지를 드롭한다")
    void preSend_subscribeFail_doesNotIncreaseSearchCount() throws Exception {
        // given
        Message<?> message = createMessage(StompCommand.SUBSCRIBE);
        willThrow(new StockQuoteException(STOCK_CODE, StockQuoteErrorType.FAIL_SUBSCRIPTION))
                .given(stockQuoteSubscriptionService).subscribe(STOCK_CODE);

        // when
        Message<?> result = stompInterceptor.preSend(message, channel);

        // then
        assertThat(result).isNull();
        verify(stockSearchService, never()).increaseSearchCount(any());
    }

    @Test
    @DisplayName("검색 빈도 증가에 실패해도 구독 메시지는 드롭되지 않는다")
    void preSend_increaseSearchCountFail_messageIsNotDropped() {
        // given
        Message<?> message = createMessage(StompCommand.SUBSCRIBE);
        willThrow(new IllegalStateException("DB 오류")).given(stockSearchService).increaseSearchCount(STOCK_CODE);

        // when
        Message<?> result = stompInterceptor.preSend(message, channel);

        // then
        assertThat(result).isSameAs(message);
    }

    @Test
    @DisplayName("구독 해제 시에는 검색 빈도를 증가시키지 않는다")
    void preSend_unsubscribe_doesNotIncreaseSearchCount() {
        // given
        Message<?> message = createMessage(StompCommand.UNSUBSCRIBE);

        // when
        stompInterceptor.preSend(message, channel);

        // then
        verify(stockSearchService, never()).increaseSearchCount(any());
    }

    private Message<?> createMessage(StompCommand command) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setSessionId("session-1");
        accessor.setNativeHeader("stockCode", STOCK_CODE);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
