package muzusi.application.kis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.kis.dto.KisDto;
import muzusi.application.stock.dto.StockRankDto;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import muzusi.infrastructure.kis.KisRankingClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisStockRankingUpdater {
    private final KisRankingClient kisRankingClient;
    private final RedisService redisService;
    
    /**
     * 거래량 기준 주식 순위를 저장하는 메서드
     * - 한국투자증권 주식 거래량 순위 api 호출
     * - 주식 순위는 Redis 내 저장
     */
    public void saveVolumeRank() {
        List<StockRankDto> stockRankDtos = kisRankingClient.getVolumeRank();

        redisService.del(KisConstant.VOLUME_RANK_PREFIX.getValue());

        for (StockRankDto stockRankDto : stockRankDtos) {
            redisService.setList(KisConstant.VOLUME_RANK_PREFIX.getValue(), stockRankDto);
        }

        redisService.set(KisConstant.VOLUME_RANK_TIME_PREFIX.getValue(), KisDto.Time.of(LocalDateTime.now()));
    }
    
    /**
     * 급상승/급하락 주식 순위를 저장하는 메서드
     * - 한국투자증권 급상승/급하락 주식 순위 api 호출
     * - 주식 순위는 Redis 내 저장
     */
    public void saveFluctuationRank() {
        List<StockRankDto> risingRankStocks = kisRankingClient.getRisingFluctuationRank();
        List<StockRankDto> fallingRankStocks = kisRankingClient.getFallingFluctuationRank();

        redisService.del(KisConstant.RISING_RANK_PREFIX.getValue());
        redisService.del(KisConstant.FALLING_RANK_PREFIX.getValue());

        for (StockRankDto risingRankStock : risingRankStocks) {
            redisService.setList(KisConstant.RISING_RANK_PREFIX.getValue(), risingRankStock);
        }

        for (StockRankDto fallingRankStock : fallingRankStocks) {
            redisService.setList(KisConstant.FALLING_RANK_PREFIX.getValue(), fallingRankStock);
        }

        redisService.set(KisConstant.FLUCTUATION_RANK_TIME_PREFIX.getValue(), KisDto.Time.of(LocalDateTime.now()));
    }
}