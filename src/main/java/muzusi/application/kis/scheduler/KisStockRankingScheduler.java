package muzusi.application.kis.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.kis.service.KisStockRankingUpdater;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KisStockRankingScheduler {
    private final KisStockRankingUpdater kisStockRankingUpdater;
    
    @Schedules({
            @Scheduled(cron = "0 0/10 9-14 * * 1-5"),
            @Scheduled(cron = "0 0,10,20,30 15 * * 1-5")
    })
    public void runRankingJobAt3PM() {
        kisStockRankingUpdater.saveVolumeRank();
        kisStockRankingUpdater.saveFluctuationRank();
    }
}
