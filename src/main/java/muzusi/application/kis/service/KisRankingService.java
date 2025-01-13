package muzusi.application.kis.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.stock.dto.FluctuationRankStockDto;
import muzusi.application.stock.dto.RankStockDto;
import muzusi.global.redis.RedisService;
import muzusi.infrastructure.kis.KisBaseClient;
import muzusi.infrastructure.kis.KisConstant;
import muzusi.infrastructure.kis.KisRankingClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisRankingService {
    private final KisRankingClient kisRankingClient;
    private final KisBaseClient kisBaseClient;
    private final RedisService redisService;

    public void saveVolumeRank() {
        List<RankStockDto> rankStockDtos = kisRankingClient.getVolumeRank();

        for (RankStockDto rankStockDto : rankStockDtos) {
            redisService.setList(KisConstant.VOLUME_RANK_PREFIX.getValue(), rankStockDto);
        }
    }

    public void saveFluctuationRank() {
        List<RankStockDto> risingRankStocks = getRankStockFromFluctuationRank(kisRankingClient.getRisingFluctuationRank());
        List<RankStockDto> fallingRankStocks = getRankStockFromFluctuationRank(kisRankingClient.getFallingFluctuationRank());

        for (RankStockDto risingRankStock : risingRankStocks) {
            redisService.setList(KisConstant.RISING_RANK_PREFIX.getValue(), risingRankStock);
        }

        for (RankStockDto fallingRankStock : fallingRankStocks) {
            redisService.setList(KisConstant.FALLING_RANK_PREFIX.getValue(), fallingRankStock);
        }
    }

    private List<RankStockDto> getRankStockFromFluctuationRank(List<FluctuationRankStockDto> fluctuationRankStockDtos) {
        List<RankStockDto> rankStockDtos = new ArrayList<>();
        int count = 0;

        for (FluctuationRankStockDto stock : fluctuationRankStockDtos) {
            if ((count + 1) % 10 == 0)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                    Thread.currentThread().interrupt();
                }
            rankStockDtos.add(RankStockDto.of(stock, kisBaseClient.getTradingValue(stock.code())));
            count++;
        }
        return rankStockDtos;
    }
}