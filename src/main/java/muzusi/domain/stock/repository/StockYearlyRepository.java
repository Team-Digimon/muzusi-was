package muzusi.domain.stock.repository;

import muzusi.domain.stock.entity.StockYearly;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StockYearlyRepository extends MongoRepository<StockYearly, String> {
    List<StockYearly> findByStockCodeOrderByDateAsc(String stockCode);

    boolean existsByDateBetween(LocalDateTime start, LocalDateTime end);
}
