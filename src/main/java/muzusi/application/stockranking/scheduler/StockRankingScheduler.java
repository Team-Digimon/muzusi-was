package muzusi.application.stockranking.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.stockranking.service.StockRankingUpdater;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockRankingScheduler {
    private final StockRankingUpdater stockRankingUpdater;
    
    @Schedules({
            @Scheduled(cron = "0 0/10 9-14 * * 1-5"),
            @Scheduled(cron = "0 0,10,20,30 15 * * 1-5")
    })
    public void runUpdateStockRankingJob() {
        stockRankingUpdater.updateVolumeRank();
        stockRankingUpdater.updateFluctuationRank();
    }
}
