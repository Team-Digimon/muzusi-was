package muzusi.global.aop;

import lombok.extern.slf4j.Slf4j;
import muzusi.global.exception.KisApiException;
import muzusi.global.exception.NewsApiException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExternalApiExceptionAspect {

    @Around("execution(* muzusi.infrastructure..*(..))")
    public Object handleServiceExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (NewsApiException e) {
            log.error("[NEWS ERROR] {}", e.getMessage());
            throw e;
        } catch (KisApiException e) {
            log.error("[KIS ERROR] {}", e.getMessage());
            throw e;
        }
    }
}
