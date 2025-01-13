package muzusi.application.kis.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.kis.service.KisOAuthService;
import muzusi.application.kis.service.KisRankingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KisScheduler {
    private final KisOAuthService kisOAuthService;
    private final KisRankingService kisRankingService;

    @Scheduled(cron = "0 0 7 * * ?")
    public void runIssueAccessTokenJob() {
        kisOAuthService.saveAccessToken();
    }

    @Scheduled(cron = "0 0 0 1 1 ?")
    public void runIssueWebSocketKeyJob() {
        kisOAuthService.saveWebSocketKey();
    }

    @Scheduled(cron = "0 0/10 9-14 * * 1-5")
    public void runRankingJob() {
        kisRankingService.saveVolumeRank();
        kisRankingService.saveFluctuationRank();
    }

    @Scheduled(cron = "0 10,20,30 15 * * 1-5")
    public void runRankingJobAt3PM() {
        kisRankingService.saveVolumeRank();
        kisRankingService.saveFluctuationRank();
    }
}