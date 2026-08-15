package muzusi.application.stockchart.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.stockchart.service.StockChartUpdater;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockChartScheduler {
    private final StockChartUpdater stockChartUpdater;

    @Schedules({
            @Scheduled(cron = "0 10,20,30,40,50 9 * * 1-5"),
            @Scheduled(cron = "0 0/10 10-14 * * 1-5"),
            @Scheduled(cron = "0 0,10,20,30 15 * * 1-5")
    })
    public void runUpdateStockMinutesChartJob() throws InterruptedException {
        stockChartUpdater.updateStockMinutesChart();
    }

    @Scheduled(cron = "0 0 16 * * 1-5")
    public void runFlushStockMinutesChartJob() {
        stockChartUpdater.flushStockMinutesChart();
    }
}
