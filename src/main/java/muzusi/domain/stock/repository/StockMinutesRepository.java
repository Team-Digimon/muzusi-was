package muzusi.domain.stock.repository;

import muzusi.domain.stock.entity.StockMinutes;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface StockMinutesRepository extends MongoRepository<StockMinutes, String> {
    List<StockMinutes> findByStockCodeOrderByDateAsc(String stockCode);

    void deleteByDateBefore(LocalDate date);
}