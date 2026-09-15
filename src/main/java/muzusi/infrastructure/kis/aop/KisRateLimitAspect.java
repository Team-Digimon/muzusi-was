package muzusi.infrastructure.kis.aop;

import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(2)
@RequiredArgsConstructor
public class KisRateLimitAspect {
    private final RateLimiter kisRateLimiter;
    
    @Around("@annotation(muzusi.infrastructure.kis.aop.KisRateLimit)")
    public Object aroundRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        kisRateLimiter.acquire();
        return joinPoint.proceed();
    }
}