package muzusi.domain.stock.repository;

import muzusi.domain.stock.entity.StockWeekly;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StockWeeklyRepository extends MongoRepository<StockWeekly, String> {
    List<StockWeekly> findByStockCodeOrderByDateAsc(String stockCode);

    boolean existsByDateBetween(LocalDateTime start, LocalDateTime end);
}
