package muzusi.infrastructure.config;

import muzusi.infrastructure.retry.GlobalRetryListener;
import muzusi.infrastructure.kis.exception.KisApiRateLimitExceedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.retry.RetryContext;
import org.springframework.retry.context.RetryContextSupport;

import static org.assertj.core.api.Assertions.assertThatCode;

class GlobalRetryListenerTest {
    private final GlobalRetryListener listener = new GlobalRetryListener();

    @Nested
    @DisplayName("재시도 실패 로깅")
    class OnError {
        @Test
        @DisplayName("재시도 중 예외가 발생해도 예외를 던지지 않고 로깅만 한다")
        void successLogsWithoutThrowing() {
            // given
            RetryContextSupport context = new RetryContextSupport(null);
            context.setAttribute(RetryContext.NAME, "test.Target.method");
            KisApiRateLimitExceedException exception = new KisApiRateLimitExceedException("유량 초과입니다.");

            // when & then
            assertThatCode(() -> listener.onError(context, null, exception))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("재시도 종료 로깅")
    class Close {
        @Test
        @DisplayName("최종 실패(throwable 존재)로 종료되어도 예외를 던지지 않고 로깅만 한다")
        void successLogsFinalFailureWithoutThrowing() {
            // given
            RetryContextSupport context = new RetryContextSupport(null);
            context.setAttribute(RetryContext.NAME, "test.Target.method");
            KisApiRateLimitExceedException exception = new KisApiRateLimitExceedException("유량 초과입니다.");

            // when & then
            assertThatCode(() -> listener.close(context, null, exception))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("최종 성공(throwable 없음)으로 종료되면 예외를 던지지 않는다")
        void successDoesNothingOnFinalSuccess() {
            // given
            RetryContextSupport context = new RetryContextSupport(null);
            context.setAttribute(RetryContext.NAME, "test.Target.method");

            // when & then
            assertThatCode(() -> listener.close(context, null, null))
                    .doesNotThrowAnyException();
        }
    }
}
