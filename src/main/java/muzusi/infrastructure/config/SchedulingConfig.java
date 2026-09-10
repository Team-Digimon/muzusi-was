package muzusi.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Slf4j
@Configuration
@EnableScheduling
public class SchedulingConfig {
    private final static int THREAD_POOL_SIZE = 3;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(THREAD_POOL_SIZE);
        taskScheduler.setThreadNamePrefix("Scheduling-thread-");
        taskScheduler.setErrorHandler(throwable -> {
            log.error("[Scheduling Error Occurred] {}", throwable.getMessage(), throwable);
        });
        taskScheduler.initialize();
        return taskScheduler;
    }
}
