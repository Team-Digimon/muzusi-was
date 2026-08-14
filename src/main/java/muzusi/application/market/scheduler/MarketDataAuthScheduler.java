package muzusi.application.market.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.market.service.MarketDataAuthService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarketDataAuthScheduler {
    private final MarketDataAuthService marketDataAuthService;
    
    @Scheduled(cron = "0 0 7 * * ?")
    public void runIssueAccessTokenJob() {
         marketDataAuthService.issueCredentials();
    }
}
