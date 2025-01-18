package muzusi.global.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.webhook.dto.Embed;
import muzusi.application.webhook.dto.Message;
import muzusi.application.webhook.service.DiscordWebhookService;
import muzusi.global.exception.KisApiException;
import muzusi.global.exception.KisOAuthApiException;
import muzusi.global.exception.NewsApiException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ExternalApiExceptionAspect {
    private final DiscordWebhookService discordWebhookService;

    @Around("execution(* muzusi.infrastructure..*(..))")
    public Object handleServiceExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (NewsApiException e) {
            log.error("[NEWS ERROR] {}", e.getMessage());
            discordWebhookService.sendDiscordWebhookMessage(getMessage("# NEWS ERROR", joinPoint, e));
            throw e;
        } catch (KisApiException e) {
            log.error("[KIS ERROR] {}", e.getMessage());
            discordWebhookService.sendDiscordWebhookMessage(getMessage("# KIS ERROR", joinPoint, e));
            throw e;
        } catch (KisOAuthApiException e) {
            log.error("[KIS ERROR] {}", e.getMessage());
            discordWebhookService.sendDiscordWebhookMessage(getCriticalMessage("# KIS ERROR - OAuth", joinPoint, e));
            return null;
        }
    }

    private Message getMessage(String title, ProceedingJoinPoint joinPoint, Exception e) {
        return Message.builder()
                .embeds(List.of(
                        Embed.builder()
                                .title(title)
                                .description(getDescription(joinPoint, e))
                                .build()
                ))
                .build();
    }

    private Message getCriticalMessage(String title, ProceedingJoinPoint joinPoint, Exception e) {
        return Message.builder()
                .embeds(List.of(
                        Embed.builder()
                                .title(title)
                                .description(getDescription(joinPoint, e))
                                .color(15548997)
                                .build()
                ))
                .build();
    }

    private String getDescription(ProceedingJoinPoint joinPoint, Exception e) {
        return new StringBuilder()
                .append("## 발생 시간\n")
                .append(LocalDateTime.now())
                .append("\n")
                .append("## 에러 정보\n")
                .append(e.getMessage())
                .append("\n")
                .append("## JoinPoint\n")
                .append("### - Class\n")
                .append(joinPoint.getSignature().getDeclaringType())
                .append("\n")
                .append("### - Method\n")
                .append(joinPoint.getSignature().getName())
                .toString();
    }
}
