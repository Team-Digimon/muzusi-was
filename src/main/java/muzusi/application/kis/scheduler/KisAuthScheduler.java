package muzusi.application.kis.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.kis.service.KisOAuthService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KisAuthScheduler {
    private final KisOAuthService kisOAuthService;
    
    @Scheduled(cron = "0 0 7 * * ?")
    public void runIssueAccessTokenJob() {
        kisOAuthService.saveAccessToken();
    }
    
    @Scheduled(cron = "0 55 8 * * ?")
    public void runIssueWebSocketKeyJob() {
        kisOAuthService.saveWebSocketKey();
    }
}
