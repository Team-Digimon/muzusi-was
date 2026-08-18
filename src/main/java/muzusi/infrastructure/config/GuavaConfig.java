package muzusi.infrastructure.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GuavaConfig {
    /**
     * 한국투자증권 REST API 호출 유량 제한: 초당 20 건
     * <p> 안전 마진을 적용하여 초당 15회의 요청만 가능하도록 제한
     */
    private static final int RATE_LIMIT = 15;

    @Bean
    public RateLimiter kisRateLimiter() {
        return RateLimiter.create(RATE_LIMIT);
    }
}
