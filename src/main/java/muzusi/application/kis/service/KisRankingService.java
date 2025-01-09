package muzusi.application.kis.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.domain.stock.entity.RankStock;
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
        List<RankStock> rankStocks = kisRankingClient.getVolumeRank();

        for(RankStock rankStock : rankStocks) {
            redisService.setList(KisConstant.VOLUME_RANK_PREFIX.getValue(), rankStock);
        }
    }
}