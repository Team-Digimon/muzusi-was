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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Async
    @Transactional
    public void processTradeReservations(String stockCode, Long stockPrice) {
        tradeReservationService.readByStockCode(stockCode).forEach(reservation -> {
            if (reservation.getTradeType() == TradeType.BUY && reservation.getInputPrice() >= stockPrice) {
                processBuyOrder(reservation);
            } else if (reservation.getTradeType() == TradeType.SELL && reservation.getInputPrice() <= stockPrice) {
                processSellOrder(reservation);
            }
        });
    }

    /**
     * 예약 매수 내역 확인 및 처리
     *
     * @param reservation : 예약 내역
     */
    private void processBuyOrder(TradeReservation reservation) {
        Account account = accountService.readByUserId(reservation.getUserId())
                .orElseThrow(() -> new CustomException(AccountErrorType.NOT_FOUND));

        Holding holding = holdingService.readByUserIdAndStockCode(reservation.getUserId(), reservation.getStockCode())
                .orElseGet(() -> createNewHolding(reservation, account));

        long price = reservation.getInputPrice() * reservation.getStockCount();
        account.clearReservedPrice(price);
        holding.addStock(reservation.getStockCount(), reservation.getInputPrice());

        finalizeTrade(reservation, account);
    }

    /**
     * 예약 매도 내역 확인 및 처리
     *
     * @param reservation : 예약 내역
     */
    private void processSellOrder(TradeReservation reservation) {
        Account account = accountService.readByUserId(reservation.getUserId())
                .orElseThrow(() -> new CustomException(AccountErrorType.NOT_FOUND));

        Holding holding = holdingService.readByUserIdAndStockCode(reservation.getUserId(), reservation.getStockCode())
                .orElseThrow(() -> new CustomException(HoldingErrorType.NOT_FOUND));

        holding.clearReservedStock(reservation.getStockCount());
        long price = reservation.getInputPrice() * reservation.getStockCount();
        account.updateAccount(reservation.getTradeType(), price);

        if (holding.isEmpty())
            holdingService.deleteByUserIdAndStockCode(reservation.getUserId(), reservation.getStockCode());

        finalizeTrade(reservation, account);
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
