package muzusi.application.stockchart.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.stockchart.service.StockChartCreationProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockChartScheduler {
    private final StockChartCreationProcessor stockChartCreationProcessor;

    @Scheduled(cron = "0 0 9 * * 1-5")
    public void runCreateStockChartJob() {
        stockChartCreationProcessor.createStockChart();
    }
}