package muzusi.application.kis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.stock.dto.RankStockDto;
import muzusi.global.redis.RedisService;
import muzusi.infrastructure.kis.KisConstant;
import muzusi.infrastructure.kis.KisRankingClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisRankingService {
    private final KisRankingClient kisRankingClient;
    private final RedisService redisService;

    public void saveVolumeRank() {
        List<RankStockDto> rankStockDtos = kisRankingClient.getVolumeRank();

        redisService.del(KisConstant.VOLUME_RANK_PREFIX.getValue());

        for (RankStockDto rankStockDto : rankStockDtos) {
            redisService.setList(KisConstant.VOLUME_RANK_PREFIX.getValue(), rankStockDto);
        }
    }

    public void saveFluctuationRank() {
        List<RankStockDto> risingRankStocks = kisRankingClient.getRisingFluctuationRank();
        List<RankStockDto> fallingRankStocks = kisRankingClient.getFallingFluctuationRank();

        redisService.del(KisConstant.RISING_RANK_PREFIX.getValue());
        redisService.del(KisConstant.FALLING_RANK_PREFIX.getValue());

        for (RankStockDto risingRankStock : risingRankStocks) {
            redisService.setList(KisConstant.RISING_RANK_PREFIX.getValue(), risingRankStock);
        }

        for (RankStockDto fallingRankStock : fallingRankStocks) {
            redisService.setList(KisConstant.FALLING_RANK_PREFIX.getValue(), fallingRankStock);
        }
    }
}