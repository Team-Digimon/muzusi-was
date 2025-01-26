package muzusi.domain.stock.repository;

import muzusi.domain.stock.entity.StockMonthly;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StockMonthlyRepository extends MongoRepository<StockMonthly, String> {
    List<StockMonthly> findByStockCode(String stockCode);
}
