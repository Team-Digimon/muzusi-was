package muzusi.application.stockchart.port;

import muzusi.application.stockcandle.dto.StockMinuteCandleDto;
import muzusi.application.stockchart.dto.StockChartDto;

import java.time.LocalDateTime;

public interface FetchStockChartPort {
    StockChartDto getStockMinutesChart(String stockCode, LocalDateTime time, int gap);
    StockMinuteCandleDto getStockMinuteCandle(String stockCode, LocalDateTime time, int gap);
}
