package muzusi.global.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.global.exception.KisApiException;
import muzusi.global.exception.KisOAuthApiException;
import muzusi.global.exception.NewsApiException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ExternalApiExceptionAspectForDev {

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
        } catch (KisOAuthApiException e) {
            log.error("[KIS OAUTH ERROR] {}", e.getMessage());
            return null;
        }
    }
}