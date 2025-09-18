package muzusi.infrastructure.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GuavaConfig {

    @Bean
    public RateLimiter kisRateLimiter() {
        return RateLimiter.create(15);
    }
}
