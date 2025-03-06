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
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.TradeConstant;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TradeReservationProcessor {
    private final TradeReservationService tradeReservationService;
    private final AccountService accountService;
    private final HoldingService holdingService;
    private final UserService userService;
    private final TradeService tradeService;
    private final RedisService redisService;

    /**
     * 예약 내역 도달 확인 및 처리 메서드
     * 1. 예약 매수/매도 별 userId로 구분하여 값 수집 (예약 체결 가능한 값)
     * 2. 예약 체결 분기별 처리
     *
     * @param stockCode : 주식 코드
     * @param lowPrice  : 특정 시간대 주식 저가
     * @param highPrice : 특정 시간대 주식 고가
     */
    @Transactional
    public void processTradeReservations(String stockCode, Long lowPrice, Long highPrice) {
        Pair<Map<Long, List<TradeReservation>>, Map<Long, List<TradeReservation>>> totalAmounts
                = calculateTotalAmounts(stockCode, lowPrice, highPrice);
        Map<Long, List<TradeReservation>> totalBuyAmountMap = totalAmounts.getLeft();
        Map<Long, List<TradeReservation>> totalSellStockMap = totalAmounts.getRight();

        processBuyOrders(totalBuyAmountMap, stockCode);
        processSellOrders(totalSellStockMap, stockCode);
    }

    /**
     * 예약 내역 중 체결 가능한 내역 매수/매도 별 userId로 구분하여 값 수집
     *
     * @param stockCode : 주식 코드
     * @param lowPrice  : 특정 시간대 주식 저가
     * @param highPrice : 특정 시간대 주식 고가
     * @return : userId별 매수/매도 Pair 쌍
     */
    private Pair<Map<Long, List<TradeReservation>>, Map<Long, List<TradeReservation>>>
    calculateTotalAmounts(
            String stockCode,
            Long lowPrice,
            Long highPrice
    ) {
        Map<Long, List<TradeReservation>> buyReservations = new HashMap<>();
        Map<Long, List<TradeReservation>> sellReservations = new HashMap<>();

        tradeReservationService.readByStockCode(stockCode).forEach(reservation -> {
            Long userId = reservation.getUserId();

            if (reservation.getTradeType() == TradeType.BUY && reservation.getInputPrice() >= lowPrice) {
                buyReservations.computeIfAbsent(userId, k -> new ArrayList<>()).add(reservation);
            } else if (reservation.getTradeType() == TradeType.SELL && reservation.getInputPrice() <= highPrice) {
                sellReservations.computeIfAbsent(userId, k -> new ArrayList<>()).add(reservation);
            }
        });

        return Pair.of(buyReservations, sellReservations);
    }

    /**
     * 예약 매수 내역 확인 및 처리
     * - userId별 계좌 및 보유 주식 조회 한 번으로 내역 업데이트
     * - 예약 체결 내역 생성 (trade)
     *
     * @param totalBuyAmountMap : 예약 내역
     * @param stockCode : 종목 코드
     */
    private void processBuyOrders(Map<Long, List<TradeReservation>> totalBuyAmountMap, String stockCode) {
        List<Long> tradeReservationIds = new ArrayList<>();
        List<Trade> trades = new ArrayList<>();

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

            reservations.forEach(reservation -> {
                tradeReservationIds.add(reservation.getId());
                trades.add(createTradeEntity(reservation, account));
            });
        });

        finalizeTrade(tradeReservationIds, trades, stockCode);
    }

    /**
     * 예약 매도 내역 확인 및 처리
     * - userId별 계좌 및 보유 주식 조회 한 번으로 내역 업데이트
     * - 예약 체결 내역 생성 (trade)
     *
     * @param totalSellStockMap : 예약 내역
     * @param stockCode : 종목 코드
     */
    private void processSellOrders(Map<Long, List<TradeReservation>> totalSellStockMap, String stockCode) {
        List<Long> tradeReservationIds = new ArrayList<>();
        List<Trade> trades = new ArrayList<>();

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

            reservations.forEach(reservation -> {
                tradeReservationIds.add(reservation.getId());
                trades.add(createTradeEntity(reservation, account));
            });
        });

        finalizeTrade(tradeReservationIds, trades, stockCode);
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
     * Trade Entity 생성
     */
    private Trade createTradeEntity(TradeReservation reservation, Account account) {
        return Trade.builder()
                .stockPrice(reservation.getInputPrice())
                .stockCount(reservation.getStockCount())
                .stockName(reservation.getStockName())
                .stockCode(reservation.getStockCode())
                .tradeType(reservation.getTradeType())
                .account(account)
                .build();
    }

    /**
     * 거래 완료 후 처리 (예약 삭제 및 거래 내역 추가)
     * 1. 예약 내역 삭제
     * 2. 거래 내역 추가
     * 3. 종목 코드에 예약 내역 없으면, redis 값 삭제
     */
    private void finalizeTrade(List<Long> tradeReservationIds, List<Trade> trades, String stockCode) {
        if (!tradeReservationIds.isEmpty()) {
            tradeReservationService.deleteAllByIds(tradeReservationIds);
        }

        if (!trades.isEmpty()) {
            tradeService.saveAll(trades);
        }

        if (!tradeReservationService.existsByStockCode(stockCode)) {
            redisService.removeFromSet(TradeConstant.RESERVATION_PREFIX.getValue(), stockCode);
        }
    }
}
