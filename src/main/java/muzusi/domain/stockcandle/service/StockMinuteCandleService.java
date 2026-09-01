package muzusi.domain.stockcandle.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stockcandle.entity.StockMinuteCandle;
import muzusi.domain.stockcandle.repository.StockMinuteCandleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMinuteCandleService {
    private final StockMinuteCandleRepository stockMinuteCandleRepository;
    
    public void saveAll(List<StockMinuteCandle> stockMinuteCandles) {
        stockMinuteCandleRepository.saveAllInBatch(stockMinuteCandles);
    }
    
    public List<StockMinuteCandle> readByStockCode(String stockCode) {
        return stockMinuteCandleRepository.findByIdStockCodeOrderByIdDateTimeAsc(stockCode);
    }
    
    public int deleteByDateTimeBefore(LocalDateTime time) {
        return stockMinuteCandleRepository.deleteByIdDateTimeBefore(time);
    }
}
