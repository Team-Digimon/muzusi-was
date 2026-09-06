package muzusi.domain.stockcandle.repository;

import muzusi.domain.stockcandle.entity.StockDailyCandle;

import java.util.List;

public interface CustomStockDailyCandleRepository {
    void saveAllInBatch(List<StockDailyCandle> stockDailyCandles);
}
