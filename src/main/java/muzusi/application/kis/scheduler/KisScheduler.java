package muzusi.application.kis.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.kis.service.KisOAuthService;
import muzusi.application.kis.service.KisRankingService;
import muzusi.application.kis.service.KisStockService;
import muzusi.domain.stock.service.StockMinutesService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class KisScheduler {
    private final KisOAuthService kisOAuthService;
    private final KisRankingService kisRankingService;
    private final KisStockService kisStockService;
    private final StockMinutesService stockMinutesService;

    @Scheduled(cron = "0 0 7 * * ?")
    public void runIssueAccessTokenJob() {
        kisOAuthService.saveAccessToken();
    }

    @Scheduled(cron = "0 0 0 1 1 ?")
    public void runIssueWebSocketKeyJob() {
        kisOAuthService.saveWebSocketKey();
    }

    @Schedules({
            @Scheduled(cron = "0 0/10 9-14 * * 1-5"),
            @Scheduled(cron = "0 10,20,30 15 * * 1-5")
    })
    public void runRankingJobAt3PM() {
        kisRankingService.saveVolumeRank();
        kisRankingService.saveFluctuationRank();
    }

    @Schedules({
            @Scheduled(cron = "0 10,20,30,40,50 9 * * 1-5"),
            @Scheduled(cron = "0 0/10 10-14 * * 1-5"),
            @Scheduled(cron = "0 0,10,20,30 15 * * 1-5")
    })
    public void runSaveStockMinutesChart() throws InterruptedException {
        kisStockService.saveStockMinutesChart();
    }

    @Scheduled(cron = "0 0 16 * * 1-5")
    public void runSaveDailyStockMinutesChartJob() {
        kisStockService.saveDailyStockMinutesChart();
        stockMinutesService.deleteByDateBefore(LocalDate.now().minusDays(6));
    }
}