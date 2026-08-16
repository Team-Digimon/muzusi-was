package muzusi.domain.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.entity.StockMonthly;
import muzusi.domain.stock.repository.StockMonthlyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMonthlyService {
    private final StockMonthlyRepository stockMonthlyRepository;

    public void save(StockMonthly stockMonthly) {
        stockMonthlyRepository.save(stockMonthly);
    }

    public List<StockMonthly> readByStockCode(String stockCode) {
        return stockMonthlyRepository.findByStockCodeOrderByDateAsc(stockCode);
    }

    public void saveAll(List<StockMonthly> stockMonthlies) {
        stockMonthlyRepository.saveAll(stockMonthlies);
    }

    public boolean existsByDateBetween(LocalDateTime start, LocalDateTime end) {
        return stockMonthlyRepository.existsByDateBetween(start, end);
    }
}
