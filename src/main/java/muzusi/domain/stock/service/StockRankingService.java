package muzusi.domain.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.repository.StockRankingCacheRepository;
import muzusi.domain.stock.type.StockRankingType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockRankingService {
    private final StockRankingCacheRepository stockRankingCacheRepository;
    
    public Map<String, Object> getRisingStockRankingInCache() {
        return stockRankingCacheRepository.findByStockRankingType(StockRankingType.RISING);
    }
    
    public Map<String, Object> getFallingStockRankingInCache() {
        return stockRankingCacheRepository.findByStockRankingType(StockRankingType.FALLING);
    }
    
    public Map<String, Object> getVolumeStockRankingInCache() {
        return stockRankingCacheRepository.findByStockRankingType(StockRankingType.VOLUME);
    }
}
