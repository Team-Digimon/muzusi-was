package muzusi.application.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.exception.AccountErrorType;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.exception.HoldingErrorType;
import muzusi.domain.holding.service.HoldingService;
import muzusi.domain.trade.entity.Trade;
import muzusi.domain.trade.entity.TradeReservation;
import muzusi.domain.trade.service.TradeReservationService;
import muzusi.domain.trade.service.TradeService;
import muzusi.domain.trade.type.TradeType;
import muzusi.domain.user.entity.User;
import muzusi.domain.user.exception.UserErrorType;
import muzusi.domain.user.service.UserService;
import muzusi.global.exception.CustomException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TradeReservationTrigger {
    private final TradeReservationService tradeReservationService;
    private final AccountService accountService;
    private final HoldingService holdingService;
    private final UserService userService;
    private final TradeService tradeService;

    /**
     * 예약 내역 도달 확인 및 처리 메서드
     *
     * @param stockCode  : 주식 코드
     * @param stockPrice : 현재 주식 가격
     */
    @Transactional
    public void processTradeReservations(String stockCode, Long stockPrice) {
        Pair<Map<Long, List<TradeReservation>>, Map<Long, List<TradeReservation>>> totalAmounts
                = calculateTotalAmounts(stockCode, stockPrice);
        Map<Long, List<TradeReservation>> totalBuyAmountMap = totalAmounts.getLeft();
        Map<Long, List<TradeReservation>> totalSellStockMap = totalAmounts.getRight();

        processBuyOrders(totalBuyAmountMap);
        processSellOrders(totalSellStockMap);
    }

    private Pair<Map<Long, List<TradeReservation>>, Map<Long, List<TradeReservation>>>
    calculateTotalAmounts(
            String stockCode,
            Long stockPrice
    ) {
        Map<Long, List<TradeReservation>> buyReservations = new HashMap<>();
        Map<Long, List<TradeReservation>> sellReservations = new HashMap<>();

        tradeReservationService.readByStockCode(stockCode).forEach(reservation -> {
            Long userId = reservation.getUserId();

            if (reservation.getTradeType() == TradeType.BUY && reservation.getInputPrice() >= stockPrice) {
                buyReservations.computeIfAbsent(userId, k -> new ArrayList<>()).add(reservation);
            } else if (reservation.getTradeType() == TradeType.SELL && reservation.getInputPrice() <= stockPrice) {
                sellReservations.computeIfAbsent(userId, k -> new ArrayList<>()).add(reservation);
            }
        });

        return Pair.of(buyReservations, sellReservations);
    }

    /**
     * 예약 매수 내역 확인 및 처리
     *
     * @param totalBuyAmountMap : 예약 내역
     */
    private void processBuyOrders(Map<Long, List<TradeReservation>> totalBuyAmountMap) {
        totalBuyAmountMap.forEach((userId, reservations) -> {
            Account account = accountService.readByUserId(userId)
                    .orElseThrow(() -> new CustomException(AccountErrorType.NOT_FOUND));

            TradeReservation firstReservation = reservations.get(0);
            Holding holding = holdingService.readByUserIdAndStockCode(userId, firstReservation.getStockCode())
                    .orElseGet(() -> createNewHolding(firstReservation, account));

            long totalPrice = reservations.stream().mapToLong(r -> r.getInputPrice() * r.getStockCount()).sum();
            int totalStockCount = reservations.stream().mapToInt(TradeReservation::getStockCount).sum();
            long averagePrice = totalPrice / totalStockCount;

            account.clearReservedPrice(totalPrice);
            holding.addStock(totalStockCount, averagePrice);

            reservations.forEach(reservation -> finalizeTrade(reservation, account));
        });
    }

    /**
     * 예약 매도 내역 확인 및 처리
     *
     * @param totalSellStockMap : 예약 내역
     */
    private void processSellOrders(Map<Long, List<TradeReservation>> totalSellStockMap) {
        totalSellStockMap.forEach((userId, reservations) -> {
            Account account = accountService.readByUserId(userId)
                    .orElseThrow(() -> new CustomException(AccountErrorType.NOT_FOUND));

            TradeReservation firstReservation = reservations.get(0);
            Holding holding = holdingService.readByUserIdAndStockCode(userId, firstReservation.getStockCode())
                    .orElseThrow(() -> new CustomException(HoldingErrorType.NOT_FOUND));

            long totalPrice = reservations.stream().mapToLong(r -> r.getInputPrice() * r.getStockCount()).sum();
            int totalStockCount = reservations.stream().mapToInt(TradeReservation::getStockCount).sum();

            holding.clearReservedStock(totalStockCount);
            account.updateAccount(TradeType.SELL, totalPrice);

            if (holding.isEmpty()) {
                holdingService.deleteByUserIdAndStockCode(userId, firstReservation.getStockCode());
            }

            reservations.forEach(reservation -> finalizeTrade(reservation, account));
        });
    }

    /**
     * Holding이 존재하지 않을 경우 새로 생성
     */
    private Holding createNewHolding(TradeReservation reservation, Account account) {
        User foundUser = userService.readById(reservation.getUserId())
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        return holdingService.save(
                Holding.builder()
                        .stockName(reservation.getStockName())
                        .stockCode(reservation.getStockCode())
                        .stockCount(0)
                        .averagePrice(0L)
                        .user(foundUser)
                        .account(account)
                        .build()
        );
    }

    /**
     * 거래 완료 후 처리 (예약 삭제 및 거래 내역 추가)
     */
    private void finalizeTrade(TradeReservation reservation, Account account) {
        tradeReservationService.deleteById(reservation.getId());

        tradeService.save(
                Trade.builder()
                        .stockPrice(reservation.getInputPrice())
                        .stockCount(reservation.getStockCount())
                        .stockName(reservation.getStockName())
                        .stockCode(reservation.getStockCode())
                        .tradeType(reservation.getTradeType())
                        .account(account)
                        .build()
        );
    }
}
