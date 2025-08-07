package muzusi.application.kis.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.kis.service.KisStockChartUpdater;
import muzusi.domain.stock.service.StockMinutesService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class KisStockChartScheduler {
    private final KisStockChartUpdater kisStockChartUpdater;
    private final StockMinutesService stockMinutesService;
    
    @Schedules({
            @Scheduled(cron = "0 10,20,30,40,50 9 * * 1-5"),
            @Scheduled(cron = "0 0/10 10-14 * * 1-5"),
            @Scheduled(cron = "0 0,10,20,30 15 * * 1-5")
    })
    public void runSaveStockMinutesChart() throws InterruptedException {
        kisStockChartUpdater.saveStockMinutesChartAndInquirePrice();
    }
    
    @Scheduled(cron = "0 0 16 * * 1-5")
    public void runSaveDailyStockMinutesChartJob() {
        kisStockChartUpdater.saveDailyStockMinutesChart();
        stockMinutesService.deleteByDateBefore(LocalDate.now().minusDays(6));
    }
}
