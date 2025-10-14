package muzusi.application.kis.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.kis.service.KisAuthUpdater;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KisAuthScheduler {
    private final KisAuthUpdater kisAuthUpdater;
    
    @Scheduled(cron = "0 0 7 * * ?")
    public void runIssueAccessTokenJob() {
        kisAuthUpdater.saveAccessToken();
    }
    
    @Scheduled(cron = "0 55 8 * * ?")
    public void runIssueWebSocketKeyJob() {
        kisAuthUpdater.saveWebSocketKeys();
    }
}
