package muzusi.application.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.service.StockRankingService;
import muzusi.domain.stock.type.StockRankingType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockRankingQueryService {
    private final StockRankingService stockRankingService;
    
    /**
     * 주식 순위 타입에 따라 해당하는 주식 순위를 조회하는 메서드
     *
     * @param stockRankingType  주식 순위 타입
     * @return                  주식 순위 및 갱신 시간 정보 Map
     */
    public Map<String, Object> getStockRankingByType(StockRankingType stockRankingType) {
        return switch (stockRankingType) {
            case FALLING -> stockRankingService.getFallingStockRankingInCache();
            case RISING -> stockRankingService.getRisingStockRankingInCache();
            case VOLUME -> stockRankingService.getVolumeStockRankingInCache();
        } ;
    }
}