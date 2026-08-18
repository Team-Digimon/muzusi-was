package muzusi.infrastructure.kis.aop;

import com.google.common.util.concurrent.RateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KisRateLimitAspectTest {

    @Mock
    private RateLimiter kisRateLimiter;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private KisRateLimitAspect kisRateLimitAspect;

    @Nested
    @DisplayName("유량 제한 적용")
    class AroundRateLimit {
        @Test
        @DisplayName("acquire를 먼저 호출한 뒤 실제 메서드를 실행한다")
        void successAcquireBeforeProceed() throws Throwable {
            // given
            when(joinPoint.proceed()).thenReturn("result");

            // when
            Object result = kisRateLimitAspect.aroundRateLimit(joinPoint);

            // then
            InOrder order = inOrder(kisRateLimiter, joinPoint);
            order.verify(kisRateLimiter).acquire();
            order.verify(joinPoint).proceed();
            assertThat(result).isEqualTo("result");
        }

        @Test
        @DisplayName("실제 메서드의 반환값을 그대로 반환한다")
        void successReturnProceedResult() throws Throwable {
            // given
            when(joinPoint.proceed()).thenReturn(42L);

            // when
            Object result = kisRateLimitAspect.aroundRateLimit(joinPoint);

            // then
            assertThat(result).isEqualTo(42L);
        }

        @Test
        @DisplayName("실제 메서드에서 예외가 발생하면 그대로 전파한다")
        void failPropagateExceptionFromProceed() throws Throwable {
            // given
            IllegalStateException exception = new IllegalStateException("api error");
            when(joinPoint.proceed()).thenThrow(exception);

            // when & then
            assertThatThrownBy(() -> kisRateLimitAspect.aroundRateLimit(joinPoint))
                    .isSameAs(exception);
            verify(kisRateLimiter).acquire();
        }
    }
}
