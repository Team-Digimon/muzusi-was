package muzusi.domain.stock.repository;

import muzusi.domain.stock.entity.StockMonthly;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMonthlyRepository extends MongoRepository<StockMonthly, String> {
    List<StockMonthly> findByStockCodeOrderByDateAsc(String stockCode);

    boolean existsByDateBetween(LocalDateTime start, LocalDateTime end);
}
