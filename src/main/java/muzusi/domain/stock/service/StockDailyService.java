package muzusi.domain.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.entity.StockDaily;
import muzusi.domain.stock.repository.StockDailyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockDailyService {
    private final StockDailyRepository stockDailyRepository;

    public void save(StockDaily stockDaily) {
        stockDailyRepository.save(stockDaily);
    }
    public List<StockDaily> readByStockCode(String stockCode) {
        return stockDailyRepository.findByStockCode(stockCode);
    }
}
