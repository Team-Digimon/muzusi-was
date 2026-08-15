package muzusi.domain.stock.repository;

import muzusi.domain.stock.entity.StockDaily;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StockDailyRepository extends MongoRepository<StockDaily, String> {
    List<StockDaily> findByStockCodeOrderByDateAsc(String stockCode);

    boolean existsByDate(LocalDateTime date);
}
