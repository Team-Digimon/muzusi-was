package muzusi.domain.stockcandle.repository;

import muzusi.domain.stockcandle.entity.StockMinuteCandle;
import muzusi.domain.stockcandle.entity.StockMinuteCandleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMinuteCandleRepository extends JpaRepository<StockMinuteCandle, StockMinuteCandleId>, CustomStockMinuteCandleRepository {
    List<StockMinuteCandle> findByIdStockCodeOrderByIdDateTimeAsc(String stockCode);
    
    @Modifying(clearAutomatically = true)
    @Query("delete from stock_minute_candle c where c.id.dateTime < :datetime")
    int deleteByIdDateTimeBefore(@Param(value = "datetime") LocalDateTime datetime);
}
