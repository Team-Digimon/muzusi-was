package muzusi.domain.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.repository.StockPriceCacheRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockPriceService {
    private final StockPriceCacheRepository stockPriceCacheRepository;

    public void saveAllInCache(Map<String, Object> stockPriceMap) {
        stockPriceCacheRepository.saveAll(stockPriceMap);
    }
}