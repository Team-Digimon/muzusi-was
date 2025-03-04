package muzusi.application.kis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.kis.dto.KisDto;
import muzusi.application.stock.dto.StockRankDto;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import muzusi.infrastructure.kis.rest.KisRankingClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisRankingService {
    private final KisRankingClient kisRankingClient;
    private final RedisService redisService;

    public void saveVolumeRank() {
        List<StockRankDto> stockRankDtos = kisRankingClient.getVolumeRank();

        redisService.del(KisConstant.VOLUME_RANK_PREFIX.getValue());

        for (StockRankDto stockRankDto : stockRankDtos) {
            redisService.setList(KisConstant.VOLUME_RANK_PREFIX.getValue(), stockRankDto);
        }

        redisService.set(KisConstant.VOLUME_RANK_TIME_PREFIX.getValue(), KisDto.Time.of(LocalDateTime.now()));
    }

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