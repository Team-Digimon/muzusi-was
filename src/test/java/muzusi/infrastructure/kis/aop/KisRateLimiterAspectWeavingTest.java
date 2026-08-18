package muzusi.infrastructure.kis.aop;

import com.google.common.util.concurrent.RateLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KisRateLimiterAspectWeavingTest {

    @Mock
    private RateLimiter kisRateLimiter;

    @Nested
    @DisplayName("@KisRateLimit 위빙")
    class Weaving {
        @Test
        @DisplayName("@KisRateLimit이 붙은 메서드를 호출하면 acquire가 호출된다")
        void successAcquireCalledOnAnnotatedMethod() {
            // given
            TestTarget proxy = createProxy();

            // when
            proxy.annotatedMethod();

            // then
            verify(kisRateLimiter, times(1)).acquire();
        }

        @Test
        @DisplayName("@KisRateLimit이 없는 메서드를 호출하면 acquire가 호출되지 않는다")
        void successAcquireNotCalledOnPlainMethod() {
            // given
            TestTarget proxy = createProxy();

            // when
            proxy.plainMethod();

            // then
            verify(kisRateLimiter, never()).acquire();
        }

        @Test
        @DisplayName("애노테이션이 붙은 메서드를 여러 번 호출하면 호출 횟수만큼 acquire가 호출된다")
        void successAcquireCalledPerInvocation() {
            // given
            TestTarget proxy = createProxy();

            // when
            proxy.annotatedMethod();
            proxy.annotatedMethod();
            proxy.annotatedMethod();

            // then
            verify(kisRateLimiter, times(3)).acquire();
        }
    }

    private TestTarget createProxy() {
        AspectJProxyFactory factory = new AspectJProxyFactory(new TestTarget());
        factory.addAspect(new KisRateLimitAspect(kisRateLimiter));
        return factory.getProxy();
    }

    static class TestTarget {
        @KisRateLimit
        public void annotatedMethod() {
        }

        public void plainMethod() {
        }
    }
}
