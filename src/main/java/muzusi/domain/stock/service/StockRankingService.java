package muzusi.domain.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.kis.dto.KisDto;
import muzusi.application.stock.dto.StockRankDto;
import muzusi.domain.stock.repository.StockRankingCacheRepository;
import muzusi.domain.stock.type.StockRankingType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockRankingService {
    private final StockRankingCacheRepository stockRankingCacheRepository;
    
    public void setRisingRankingInCache(List<StockRankDto> values) {
        stockRankingCacheRepository.setByStockRankingType(StockRankingType.RISING, values);
    }
    
    public void setFallingRankingInCache(List<StockRankDto> values) {
        stockRankingCacheRepository.setByStockRankingType(StockRankingType.FALLING, values);
    }
    
    public void setFallingAndRisingRankingTimeInCache(KisDto.Time time) {
        stockRankingCacheRepository.setTimeByStockRankingType(StockRankingType.FALLING, time);
    }
    
    public void setVolumeRankingTimeInCache(KisDto.Time time) {
        stockRankingCacheRepository.setTimeByStockRankingType(StockRankingType.VOLUME, time);
    }
    
    public void setVolumeRankingInCache(List<StockRankDto> values) {
        stockRankingCacheRepository.setByStockRankingType(StockRankingType.VOLUME, values);
    }
    
    public Map<String, Object> getRisingStockRankingInCache() {
        return stockRankingCacheRepository.findByStockRankingType(StockRankingType.RISING);
    }
    
    public Map<String, Object> getFallingStockRankingInCache() {
        return stockRankingCacheRepository.findByStockRankingType(StockRankingType.FALLING);
    }
    
    public Map<String, Object> getVolumeStockRankingInCache() {
        return stockRankingCacheRepository.findByStockRankingType(StockRankingType.VOLUME);
    }
    
    public void deleteRisingRankingInCache() {
        stockRankingCacheRepository.deleteByStockRankingType(StockRankingType.RISING);
    }
    
    public void deleteFallingRankingInCache() {
        stockRankingCacheRepository.deleteByStockRankingType(StockRankingType.RISING);
    }
    
    public void deleteVolumeRankingCache() {
        stockRankingCacheRepository.deleteByStockRankingType(StockRankingType.VOLUME);
    }
}
