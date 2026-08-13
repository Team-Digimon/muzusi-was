package muzusi.application.stockquote.service;

import muzusi.application.stockquote.exception.StockQuoteException;
import muzusi.application.stockquote.port.StockQuotePort;
import muzusi.application.stockquote.registry.StockQuoteSubscriptionRegistry;
import muzusi.application.stockquote.registry.StockQuoteSubscriptionResult;
import muzusi.domain.stockquote.exception.StockQuoteErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockQuoteSubscriptionServiceTest {

    @Mock
    private StockQuotePort stockQuotePort;

    @Mock
    private StockQuoteSubscriptionRegistry registry;

    @InjectMocks
    private StockQuoteSubscriptionService stockQuoteSubscriptionService;

    @Nested
    @DisplayName("구독 초기화")
    class SetupSubscription {
        @Test
        @DisplayName("연동 포트로 연결된 세션 ID 목록을 레지스트리 초기화에 그대로 전달한다")
        void successSetupSubscription() {
            // given
            List<String> connectionIds = List.of("session1", "session2");
            when(stockQuotePort.connect()).thenReturn(connectionIds);

            // when
            stockQuoteSubscriptionService.setupSubscription();

            // then
            verify(registry).initialize(connectionIds);
        }
    }

    @Nested
    @DisplayName("구독 초기화 해제")
    class ResetSubscription {
        @Test
        @DisplayName("레지스트리를 초기화 해제한다")
        void successResetSubscription() {
            // when
            stockQuoteSubscriptionService.resetSubscription();

            // then
            verify(registry).reset();
        }
    }

    @Nested
    @DisplayName("구독")
    class Subscribe {
        private final String stockCode = "000660";
        private final String sessionId = "session1";

        @Test
        @DisplayName("레지스트리 구독이 실패하면 StockQuoteException을 던지고 연동 포트에는 요청하지 않는다")
        void failSubscribeWhenRegistryFails() throws InterruptedException {
            // given
            StockQuoteSubscriptionResult.Subscription result =
                    new StockQuoteSubscriptionResult.Subscription(null, false, false);
            when(registry.subscribe(stockCode)).thenReturn(result);

            // when & then
            assertThatThrownBy(() -> stockQuoteSubscriptionService.subscribe(stockCode))
                    .isInstanceOf(StockQuoteException.class)
                    .extracting(e -> ((StockQuoteException) e).getErrorType())
                    .isEqualTo(StockQuoteErrorType.FAIL_SUBSCRIPTION);

            verify(stockQuotePort, never()).subscribe(anyString(), anyString());
        }

        @Test
        @DisplayName("신규 구독에 성공하면 연동 포트로 실제 구독을 요청한다")
        void successNewSubscription() throws InterruptedException {
            // given
            StockQuoteSubscriptionResult.Subscription result =
                    new StockQuoteSubscriptionResult.Subscription(sessionId, true, true);
            when(registry.subscribe(stockCode)).thenReturn(result);

            // when
            stockQuoteSubscriptionService.subscribe(stockCode);

            // then
            verify(stockQuotePort, times(1)).subscribe(sessionId, stockCode);
        }

        @Test
        @DisplayName("이미 구독 중인 종목의 재구독은 연동 포트에 별도로 요청하지 않는다")
        void successReSubscriptionSkipsPortRequest() throws InterruptedException {
            // given
            StockQuoteSubscriptionResult.Subscription result =
                    new StockQuoteSubscriptionResult.Subscription(sessionId, true, false);
            when(registry.subscribe(stockCode)).thenReturn(result);

            // when
            stockQuoteSubscriptionService.subscribe(stockCode);

            // then
            verify(stockQuotePort, never()).subscribe(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("구독 해제")
    class Unsubscribe {
        private final String stockCode = "000660";
        private final String sessionId = "session1";

        @Test
        @DisplayName("레지스트리 구독 해제가 실패하면 StockQuoteException을 던지고 연동 포트에는 요청하지 않는다")
        void failUnsubscribeWhenRegistryFails() throws InterruptedException {
            // given
            StockQuoteSubscriptionResult.Unsubscription result =
                    new StockQuoteSubscriptionResult.Unsubscription(null, false, false);
            when(registry.unsubscribe(stockCode)).thenReturn(result);

            // when & then
            assertThatThrownBy(() -> stockQuoteSubscriptionService.unsubscribe(stockCode))
                    .isInstanceOf(StockQuoteException.class)
                    .extracting(e -> ((StockQuoteException) e).getErrorType())
                    .isEqualTo(StockQuoteErrorType.FAIL_UNSUBSCRIPTION);

            verify(stockQuotePort, never()).unsubscribe(anyString(), anyString());
        }

        @Test
        @DisplayName("마지막 구독까지 해제되면 연동 포트로 실제 구독 해제를 요청한다")
        void successFullyUnsubscribed() throws InterruptedException {
            // given
            StockQuoteSubscriptionResult.Unsubscription result =
                    new StockQuoteSubscriptionResult.Unsubscription(sessionId, true, true);
            when(registry.unsubscribe(stockCode)).thenReturn(result);

            // when
            stockQuoteSubscriptionService.unsubscribe(stockCode);

            // then
            verify(stockQuotePort, times(1)).unsubscribe(sessionId, stockCode);
        }

        @Test
        @DisplayName("다른 구독이 남아있으면 연동 포트에 별도로 요청하지 않는다")
        void successPartialUnsubscribeSkipsPortRequest() throws InterruptedException {
            // given
            StockQuoteSubscriptionResult.Unsubscription result =
                    new StockQuoteSubscriptionResult.Unsubscription(sessionId, true, false);
            when(registry.unsubscribe(stockCode)).thenReturn(result);

            // when
            stockQuoteSubscriptionService.unsubscribe(stockCode);

            // then
            verify(stockQuotePort, never()).unsubscribe(anyString(), anyString());
        }
    }
}

