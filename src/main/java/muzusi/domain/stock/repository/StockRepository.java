package muzusi.domain.stock.repository;

import muzusi.domain.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByStockName(String stockName);
    Optional<Stock> findByStockCode(String stockCode);
    boolean existsByStockCode(String stockCode);
}