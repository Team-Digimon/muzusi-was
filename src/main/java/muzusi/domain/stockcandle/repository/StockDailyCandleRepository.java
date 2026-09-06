package muzusi.domain.stockcandle.repository;

import muzusi.domain.stockcandle.entity.StockDailyCandle;
import muzusi.domain.stockcandle.entity.StockDailyCandleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StockDailyCandleRepository extends JpaRepository<StockDailyCandle, StockDailyCandleId>, CustomStockDailyCandleRepository {
    boolean existsByIdDate(LocalDate date);
    
    List<StockDailyCandle> findByIdStockCodeAndIdDateBetween(String stockCode, LocalDate from, LocalDate to);
}
