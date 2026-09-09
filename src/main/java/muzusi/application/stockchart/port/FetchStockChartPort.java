package muzusi.application.stockchart.port;

import muzusi.application.stockcandle.dto.StockMinuteCandleDto;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FetchStockChartPort {
    Optional<StockMinuteCandleDto> getStockMinuteCandle(String stockCode, LocalDateTime time, int gap);
}
