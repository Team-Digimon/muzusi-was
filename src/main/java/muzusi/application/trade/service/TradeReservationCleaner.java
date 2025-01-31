package muzusi.application.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.exception.AccountErrorType;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.exception.HoldingErrorType;
import muzusi.domain.holding.service.HoldingService;
import muzusi.domain.trade.service.TradeReservationService;
import muzusi.domain.trade.type.TradeType;
import muzusi.global.exception.CustomException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TradeReservationCleaner {
    private final TradeReservationService tradeReservationService;
    private final AccountService accountService;
    private final HoldingService holdingService;

    /**
     * 주식 장 마감 후, 미처리 예약 삭제 메서드
     * 1. 미처리 예약 매수/매도 별 userId로 구분하여 값 수집
     * 2. 매수/매도 별 userId를 통해 예약 취소 처리
     * 3. 예약 내역 삭제
     */
    @Transactional
    public void clearReservedOrdersAtMarketClose() {
        Pair<Map<Long, Long>, Map<Long, Map<String, Integer>>> totalAmounts = calculateTotalAmounts();
        Map<Long, Long> totalBuyAmountMap = totalAmounts.getLeft();
        Map<Long, Map<String, Integer>> totalSellStockMap = totalAmounts.getRight();

        discardReservedBuyAmounts(totalBuyAmountMap);
        discardReservedSellStocks(totalSellStockMap);

        tradeReservationService.deleteAll();
    }

    /**
     * userId 및 매수/매도 별 예약 내역 값 분리
     *
     * @return 왼 : 예약 매수 값 분리. 오 : 예약 매도 값 분리.
     */
    private Pair<Map<Long, Long>, Map<Long, Map<String, Integer>>> calculateTotalAmounts() {
        Map<Long, Long> totalBuyAmountMap = new HashMap<>();
        Map<Long, Map<String, Integer>> totalSellStockMap = new HashMap<>();

        tradeReservationService.readAll().forEach(reservation -> {
            Long userId = reservation.getUserId();
            if (reservation.getTradeType() == TradeType.BUY) {
                totalBuyAmountMap.merge(
                        userId,
                        reservation.getInputPrice() * reservation.getStockCount(),
                        Long::sum
                );
            } else if (reservation.getTradeType() == TradeType.SELL) {
                totalSellStockMap.computeIfAbsent(userId, k -> new HashMap<>())
                        .merge(reservation.getStockCode(), reservation.getStockCount(), Integer::sum);

            }
        });

        return Pair.of(totalBuyAmountMap, totalSellStockMap);
    }

    /**
     * 예약 매수 내역 취소 처리
     *
     * @param totalBuyAmountMap : key - userId, value - 예약 매수 내역
     */
    private void discardReservedBuyAmounts(Map<Long, Long> totalBuyAmountMap) {
        totalBuyAmountMap.forEach((userId, totalBuyAmount) -> {
            Account account = accountService.readByUserId(userId)
                    .orElseThrow(() -> new CustomException(AccountErrorType.NOT_FOUND));
            account.decreaseReservedPrice(totalBuyAmount);
        });
    }

    /**
     * 예약 매도 내역 취소 처리
     *
     * @param totalSellStockMap : key - userId, value - 예약 매도 내역
     */
    private void discardReservedSellStocks(Map<Long, Map<String, Integer>> totalSellStockMap) {
        totalSellStockMap.forEach((userId, stockCounts) ->
                stockCounts.forEach((stockCode, totalStockCount) -> {
                    Holding holding = holdingService.readByUserIdAndStockCode(userId, stockCode)
                            .orElseThrow(() -> new CustomException(HoldingErrorType.NOT_FOUND));
                    holding.decreaseReservedStock(totalStockCount);
                })
        );
    }
}
