package muzusi.infrastructure.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GlobalRetryListener implements RetryListener {
    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        log.warn(
                "[Retry] {}번째 시도 실패 (target: {}): {}",
                context.getRetryCount(),
                context.getAttribute(RetryContext.NAME),
                throwable.getMessage()
        );
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        if (throwable != null) {
            log.error(
                    "[Retry] {}회 재시도 후 최종 실패 (target: {})",
                    context.getRetryCount(),
                    context.getAttribute(RetryContext.NAME),
                    throwable
            );
        }
    }
}
