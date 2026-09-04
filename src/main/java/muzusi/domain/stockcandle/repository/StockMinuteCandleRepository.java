package muzusi.domain.stockcandle.repository;

import muzusi.domain.stockcandle.dto.StockMinuteCandleDto;
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
    
    @Query(value = """
            SELECT new muzusi.domain.stockcandle.dto.StockMinuteCandleDto(
                            smc.id.stockCode, smc.id.dateTime, smc.open, smc.high, smc.low, smc.close, smc.volume
                    )
            FROM stock_minute_candle smc
            WHERE smc.id.dateTime >= :dateTime
            ORDER BY smc.id.stockCode, smc.id.dateTime
        """
    )
    List<StockMinuteCandleDto> findStockMinuteCandleDtoByDateTimeGreaterThanEqual(@Param(value = "dateTime") LocalDateTime dateTime);
}
