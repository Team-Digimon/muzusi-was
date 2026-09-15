package muzusi.infrastructure.config;

import muzusi.infrastructure.kis.constant.KisRetryConstant;
import muzusi.infrastructure.kis.exception.KisApiException;
import muzusi.infrastructure.kis.exception.KisApiRateLimitExceedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KisRetryWeavingTest {
    @Nested
    @DisplayName("유량 초과 예외 재시도")
    class RetryOnRateLimitExceeded {
        @Test
        @DisplayName("최대 시도 횟수까지 재시도하고, 계속 실패하면 마지막 예외를 던진다")
        void failThrowsAfterMaxAttempts() {
            // given
            TestTarget target = new TestTarget(Integer.MAX_VALUE, false);
            TestTarget proxy = createProxy(target, null);

            // when & then
            assertThatThrownBy(proxy::call)
                    .isInstanceOf(KisApiRateLimitExceedException.class);
            assertThat(target.getCallCount()).isEqualTo(KisRetryConstant.MAX_ATTEMPTS);
        }

        @Test
        @DisplayName("최대 시도 횟수 이전에 성공하면 재시도를 멈추고 결과를 반환한다")
        void successStopsRetryingOnceSucceeded() {
            // given
            int succeedOnAttempt = KisRetryConstant.MAX_ATTEMPTS - 1;
            TestTarget target = new TestTarget(succeedOnAttempt, false);
            TestTarget proxy = createProxy(target, null);

            // when
            String result = proxy.call();

            // then
            assertThat(result).isEqualTo("success");
            assertThat(target.getCallCount()).isEqualTo(succeedOnAttempt);
        }

        @Test
        @DisplayName("유량 초과가 아닌 예외는 재시도하지 않고 즉시 전파한다")
        void failDoesNotRetryOnOtherException() {
            // given
            TestTarget target = new TestTarget(Integer.MAX_VALUE, true);
            TestTarget proxy = createProxy(target, null);

            // when & then
            assertThatThrownBy(proxy::call)
                    .isInstanceOf(KisApiException.class)
                    .isNotInstanceOf(KisApiRateLimitExceedException.class);
            assertThat(target.getCallCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("재시도 리스너 연동")
    class ListenerIntegration {

        @Test
        @DisplayName("재시도가 실패할 때마다 리스너의 onError가 호출되고, 종료 시 close가 호출된다")
        void successListenerInvokedOnEachFailureAndOnClose() {
            // given
            RetryListener listener = mock(RetryListener.class);
            when(listener.open(any(), any())).thenReturn(true);
            TestTarget target = new TestTarget(Integer.MAX_VALUE, false);
            TestTarget proxy = createProxy(target, listener);

            // when
            assertThatThrownBy(proxy::call);

            // then
            verify(listener, times(KisRetryConstant.MAX_ATTEMPTS)).onError(any(), any(), any());
            verify(listener, times(1)).close(any(), any(), any());
        }
    }

    private TestTarget createProxy(TestTarget target, RetryListener listener) {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(
                KisRetryConstant.MAX_ATTEMPTS,
                Map.of(KisApiRateLimitExceedException.class, true)
        ));

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1L);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        if (listener != null) {
            retryTemplate.setListeners(new RetryListener[]{listener});
        }

        ProxyFactory factory = new ProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAdvice(RetryInterceptorBuilder.stateless()
                .retryOperations(retryTemplate)
                .build());

        return (TestTarget) factory.getProxy();
    }

    static class TestTarget {
        private final int succeedOnAttempt;
        private final boolean throwsOtherException;
        private final AtomicInteger callCount = new AtomicInteger();

        TestTarget(int succeedOnAttempt, boolean throwsOtherException) {
            this.succeedOnAttempt = succeedOnAttempt;
            this.throwsOtherException = throwsOtherException;
        }

        public String call() {
            int attempt = callCount.incrementAndGet();

            if (throwsOtherException) {
                throw new KisApiException("다른 종류의 에러입니다.");
            }
            if (attempt < succeedOnAttempt) {
                throw new KisApiRateLimitExceedException("유량 초과입니다.");
            }
            return "success";
        }

        public int getCallCount() {
            return callCount.get();
        }
    }
}
