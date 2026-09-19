package muzusi.domain.stocksearch.repository;

import muzusi.domain.stocksearch.entity.StockSearchStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockSearchStatRepository extends JpaRepository<StockSearchStat, Long> {
    List<StockSearchStat> findByStockCodeIn(List<String> stockCodes);
}
