package muzusi.application.kis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.kis.dto.KisDto;
import muzusi.application.stock.dto.StockRankDto;
import muzusi.domain.stock.service.StockRankingService;
import muzusi.infrastructure.kis.ranking.KisRankingClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisStockRankingUpdater {
    private final KisRankingClient kisRankingClient;
    private final StockRankingService stockRankingService;
    
    /**
     * 거래량 기준 주식 순위를 저장하는 메서드
     * - 한국투자증권 주식 거래량 순위 api 호출
     * - 주식 순위는 Redis 내 저장
     */
    public void saveVolumeRank() {
        List<StockRankDto> stockRanking = kisRankingClient.getVolumeRank();

        stockRankingService.deleteVolumeRankingCache();

        stockRankingService.setVolumeRankingInCache(stockRanking);
        
        stockRankingService.setVolumeRankingTimeInCache(new KisDto.Time(LocalDateTime.now()));
    }
    
    /**
     * 급상승/급하락 주식 순위를 저장하는 메서드
     * - 한국투자증권 급상승/급하락 주식 순위 api 호출
     * - 주식 순위는 Redis 내 저장
     */
    public void saveFallingAndRisingRank() {
        List<StockRankDto> risingStockRanking = kisRankingClient.getRisingFluctuationRank();
        List<StockRankDto> fallingStockRanking = kisRankingClient.getFallingFluctuationRank();

        stockRankingService.deleteRisingRankingInCache();
        stockRankingService.deleteFallingRankingInCache();

        stockRankingService.setRisingRankingInCache(risingStockRanking);
        stockRankingService.setFallingRankingInCache(fallingStockRanking);
        stockRankingService.setFallingAndRisingRankingTimeInCache(new KisDto.Time(LocalDateTime.now()));
    }
}