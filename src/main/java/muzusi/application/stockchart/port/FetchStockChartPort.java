package muzusi.application.stockchart.port;

import muzusi.application.stockcandle.dto.StockMinuteCandleDto;

import java.time.LocalDateTime;

public interface FetchStockChartPort {
    StockMinuteCandleDto getStockMinuteCandle(String stockCode, LocalDateTime time, int gap);
}
