package muzusi.domain.stockcandle.repository;

import muzusi.domain.stockcandle.entity.StockMinuteCandle;

import java.util.List;

public interface CustomStockMinuteCandleRepository {
    void saveAllInBatch(List<StockMinuteCandle> stockMinuteCandles);
}
