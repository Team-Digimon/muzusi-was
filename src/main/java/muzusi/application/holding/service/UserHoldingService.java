package muzusi.application.holding.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.account.dto.AccountSummaryDto;
import muzusi.application.holding.dto.HoldingInfoDto;
import muzusi.application.stock.dto.StockPriceDto;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.service.HoldingService;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserHoldingService {
    private final HoldingService holdingService;
    private final RedisService redisService;

    /**
     * 사용자의 보유 주식 목록을 가져오는 메서드
     *
     * @param userId : 사용자 ID
     * @return : 보유 주식 목록 (HoldingInfoDto 리스트)
     */
    @Transactional(readOnly = true)
    public List<HoldingInfoDto> getUserHoldings(Long userId) {
        List<Holding> holdings = holdingService.readByUserId(userId);

        if (holdings.isEmpty())
            return Collections.emptyList();

        Map<String, Object> stockPrices = getStockPrices(holdings);

        return holdings.stream()
                .map(holding -> mapToHoldingInfoDto(holding, stockPrices))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 보유 종목들에 대한 총 수익률을 계산하는 메서드
     *
     * @param userId : 사용자 ID
     * @return : 총 수익률, 평가금액, 수익금액
     */
    @Transactional(readOnly = true)
    public AccountSummaryDto calculateTotalRateOfReturn(Long userId) {
        List<Holding> holdings = holdingService.readByUserId(userId);
        if (holdings.isEmpty()) return AccountSummaryDto.of(0.0, 0, 0);

        Map<String, Object> stockPrices = getStockPrices(holdings);

        long totalPurchaseAmount = holdings.stream()
                .mapToLong(h -> h.getAveragePrice() * h.getStockCount())
                .sum();

        long totalEvaluatedAmount = holdings.stream()
                .mapToLong(h -> {
                    Object priceDto = stockPrices.get(h.getStockCode());
                    return (priceDto instanceof StockPriceDto stockPriceDto)
                            ? stockPriceDto.close() * h.getStockCount()
                            : 0;
                })
                .sum();

        long totalProfitAmount = totalEvaluatedAmount - totalPurchaseAmount;

        double totalRateOfReturn = (totalPurchaseAmount == 0) ? 0.0
                : ((double) totalProfitAmount / totalPurchaseAmount) * 100;

        return AccountSummaryDto.of(
                Math.round(totalRateOfReturn * 100) / 100.0,
                totalEvaluatedAmount,
                totalProfitAmount
        );
    }

    /**
     * Redis에서 주식 현재가 불러오기 메서드
     *
     * @param holdings : 보유 주식들
     * @return : 주식(Key)별 현재가(value)
     */
    private Map<String, Object> getStockPrices(List<Holding> holdings) {
        List<String> stockCodes = holdings.stream()
                .map(Holding::getStockCode)
                .distinct()
                .toList();

        return redisService.getHashMultiple(KisConstant.INQUIRE_PRICE_PREFIX.getValue(), stockCodes);
    }

    /**
     * Holding 객체를 HoldingInfoDto로 변환하는 메서드
     *
     * @param holding : 보유 주식 정보
     * @param stockPrices : Redis에서 가져온 주식 가격 정보 Map
     * @return : HoldingInfoDto (null 값 제거됨)
     */
    private HoldingInfoDto mapToHoldingInfoDto(Holding holding, Map<String, Object> stockPrices) {
        Object priceDto = stockPrices.get(holding.getStockCode());
        if (!(priceDto instanceof StockPriceDto stockPriceDto)) return null;

        long profitAmountPerStock = stockPriceDto.close() - holding.getAveragePrice();
        double rateOfReturn = ((double) profitAmountPerStock / holding.getAveragePrice()) * 100;
        long totalProfitAmount = profitAmountPerStock * holding.getStockCount();

        return HoldingInfoDto.from(
                holding,
                Math.round(rateOfReturn * 100) / 100.0,
                totalProfitAmount
        );
    }
}

