package muzusi.domain.stocksearch.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stocksearch.entity.StockSearchStat;
import muzusi.domain.stocksearch.repository.StockSearchStatRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockSearchStatService {
    private final StockSearchStatRepository stockSearchStatRepository;
    
    public void increaseSearchCount(String stockCode) {
        stockSearchStatRepository.increaseSearchCount(stockCode);
    }
    
    public List<StockSearchStat> readByStockCodeIn(List<String> stockCodes) {
        return stockSearchStatRepository.findByStockCodeIn(stockCodes);
    }
}
