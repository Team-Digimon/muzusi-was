package muzusi.application.stock.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.service.StockChartService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockScheduler {
    private final StockChartService stockChartService;

    @Scheduled(cron = "0 0 9 * * 1-5")
    public void runCreateStockChartJob() {
        stockChartService.createStockChart();
    }
}