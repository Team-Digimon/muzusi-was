package muzusi.application.kis.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.RankStockDto;
import muzusi.global.redis.RedisService;
import muzusi.infrastructure.kis.KisConstant;
import muzusi.infrastructure.kis.KisRankingClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KisRankingService {
    private final KisRankingClient kisRankingClient;
    private final RedisService redisService;

    public void saveVolumeRank() {
        List<RankStockDto> rankStockDtos = kisRankingClient.getVolumeRank();

        for (RankStockDto rankStockDto : rankStockDtos) {
            redisService.setList(KisConstant.VOLUME_RANK_PREFIX.getValue(), rankStockDto);
        }
    }
}