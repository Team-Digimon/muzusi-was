package muzusi.application.stockranking.service;

import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.StockRankDto;
import muzusi.application.stockranking.port.FetchStockRankingPort;
import muzusi.domain.stock.service.StockRankingService;
import muzusi.global.util.datetime.DateTimeFormatterUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockRankingUpdater {
    private final FetchStockRankingPort fetchStockRankingPort;
    private final StockRankingService stockRankingService;
    private final RateLimiter kisRateLimiter; // TODO: 향후 infrastructure 레이어로 이동

    /**
     * 주식 순위 포트를 통한 주식 거래량 순위 데이터 수집 후, 주식 거래량 순위를 갱신하는 메서드
     *
     * <p> 주식 거래량 순위 데이터를 수집한 다음 기존 데이터를 제거한 후 갱신한다.
     * <p> 주식 거래량 순위를 저장할 때, 갱신 시간도 같이 저장한다.
     */
    public void updateVolumeRank() {
        kisRateLimiter.acquire();

        List<StockRankDto> volumeRank = fetchStockRankingPort.getVolumeRank();
        stockRankingService.deleteVolumeRankingCache();
        stockRankingService.setVolumeRankingInCache(volumeRank);
        stockRankingService.setVolumeRankingTimeInCache(DateTimeFormatterUtil.parseToString(LocalDateTime.now()));
    }

    /**
     * 주식 순위 포트를 통한 주식 급등락 순위 데이터 수집 후, 주식 급등락 순위를 갱신하는 메서드
     *
     * <p> 주식 급등락 순위 데이터를 수집한 다음 기존 데이터를 제거한 후 갱신한다.
     * <p> 주식 급등락 순위를 저장할 때, 갱신 시간도 같이 저장한다.
     */
    public void updateFluctuationRank() {
        kisRateLimiter.acquire();

        List<StockRankDto> risingStockRanking = fetchStockRankingPort.getRisingFluctuationRank();
        List<StockRankDto> fallingStockRanking = fetchStockRankingPort.getFallingFluctuationRank();

        stockRankingService.deleteRisingRankingInCache();
        stockRankingService.deleteFallingRankingInCache();

        stockRankingService.setRisingRankingInCache(risingStockRanking);
        stockRankingService.setFallingRankingInCache(fallingStockRanking);
        stockRankingService.setFallingAndRisingRankingTimeInCache(DateTimeFormatterUtil.parseToString(LocalDateTime.now()));
    }
}
