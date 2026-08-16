package muzusi.application.stockchart.port;

import muzusi.application.stockchart.dto.StockChartDto;

import java.time.LocalDateTime;

public interface FetchStockChartPort {
    StockChartDto getStockMinutesChart(String stockCode, LocalDateTime time, int gap);

}
