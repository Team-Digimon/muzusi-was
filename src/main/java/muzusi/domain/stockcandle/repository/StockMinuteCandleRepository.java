package muzusi.domain.stockcandle.repository;

import muzusi.domain.stockcandle.entity.StockMinuteCandle;
import muzusi.domain.stockcandle.entity.StockMinuteCandleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMinuteCandleRepository extends JpaRepository<StockMinuteCandle, StockMinuteCandleId>, CustomStockMinuteCandleRepository {
    List<StockMinuteCandle> findByIdStockCodeOrderByIdDateTimeAsc(String stockCode);
}
