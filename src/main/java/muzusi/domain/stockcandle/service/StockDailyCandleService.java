package muzusi.domain.stockcandle.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stockcandle.entity.StockDailyCandle;
import muzusi.domain.stockcandle.repository.StockDailyCandleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockDailyCandleService {
    private final StockDailyCandleRepository stockDailyCandleRepository;
    
    public void saveAll(List<StockDailyCandle> stockDailyCandles) {
        stockDailyCandleRepository.saveAllInBatch(stockDailyCandles);
    }
    
    public List<StockDailyCandle> readByStockCodeBetween(String stockCode, LocalDate from, LocalDate to) {
        return stockDailyCandleRepository.findByIdStockCodeAndIdDateBetween(stockCode, from, to);
    }
    
    public boolean existsByDate(LocalDate date) {
        return stockDailyCandleRepository.existsByIdDate(date);
    }
}
