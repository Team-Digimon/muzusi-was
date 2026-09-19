package muzusi.domain.stocksearch.repository;

import muzusi.domain.stocksearch.entity.StockSearchStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockSearchStatRepository extends JpaRepository<StockSearchStat, Long> {
    List<StockSearchStat> findByStockCodeIn(List<String> stockCodes);
    
    @Modifying
    @Query("UPDATE StockSearchStat s SET s.searchCount = s.searchCount + 1 WHERE s.stockCode = :stockCode")
    int increaseSearchCount(@Param(value = "stockCode") String stockCode);
}
