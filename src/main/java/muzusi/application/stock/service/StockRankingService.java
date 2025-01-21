package muzusi.application.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.RankStockDto;
import muzusi.domain.stock.type.StockRankingType;
import muzusi.global.redis.RedisService;
import muzusi.infrastructure.kis.KisConstant;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockRankingService {
    private final RedisService redisService;

    public List<RankStockDto> getStockRanking(StockRankingType stockRankingType) {
        return switch (stockRankingType) {
            case RISING -> getStockRankingByType(KisConstant.RISING_RANK_PREFIX);
            case FALLING -> getStockRankingByType(KisConstant.FALLING_RANK_PREFIX);
            case VOLUME -> getStockRankingByType(KisConstant.VOLUME_RANK_PREFIX);
        };
    }

    private List<RankStockDto> getStockRankingByType(KisConstant kisConstant) {
        return redisService.getList(kisConstant.getValue()).stream().map(obj -> (RankStockDto) obj).toList();
    }
}