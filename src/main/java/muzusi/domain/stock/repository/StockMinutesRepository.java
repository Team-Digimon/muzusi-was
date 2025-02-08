package muzusi.domain.stock.repository;

import muzusi.domain.stock.entity.StockMinutes;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface StockMinutesRepository extends CrudRepository<StockMinutes, Integer> {
    List<StockMinutes> findByStockCodeOrderByDateAsc(String stockCode);

    void deleteByDateBefore(LocalDate date);
}