package muzusi.application.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.trade.dto.TradeReqDto;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.exception.AccountErrorType;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.exception.HoldingErrorType;
import muzusi.domain.holding.service.HoldingService;
import muzusi.domain.trade.entity.TradeReservation;
import muzusi.domain.trade.exception.TradeErrorType;
import muzusi.domain.trade.service.TradeReservationService;
import muzusi.global.exception.CustomException;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.TradeConstant;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeReservationHandler {
    private final TradeReservationService tradeReservationService;
    private final AccountService accountService;
    private final HoldingService holdingService;
    private final RedisService redisService;

    /**
     * 성사가 안된 주식 거래를 예약 처리하는 메서드
     *
     * @param userId : 사용자 pk값
     * @param tradeReqDto : trade 정보 dto
     */
    public void saveTradeReservation(Long userId, TradeReqDto tradeReqDto) {
        switch (tradeReqDto.tradeType()) {
            case BUY -> handleReservationPurchase(tradeReqDto, userId);
            case SELL -> handleReservationSale(tradeReqDto, userId);
            default -> throw new IllegalArgumentException("잘못된 거래 유형입니다.");
        }

        finalizeSaveReservation(userId, tradeReqDto);
    }

    /**
     * 매수 예약 처리 메서드
     * 계좌 잔액을 차감한다.
     */
    private void handleReservationPurchase(TradeReqDto tradeReqDto, Long userId) {
        Account account = accountService.readByUserId(userId)
                .orElseThrow(() -> new CustomException(AccountErrorType.NOT_FOUND));

        long price = tradeReqDto.inputPrice() * tradeReqDto.stockCount();

        if (account.getBalance() < price)
            throw new CustomException(AccountErrorType.INSUFFICIENT_BALANCE);

        account.increaseReservedPrice(price);
    }

    /**
     * 매도 예약 처리
     * 사용자가 보유한 주식 수량을 차감한다
     */
    private void handleReservationSale(TradeReqDto tradeReqDto, Long userId) {
        Holding holding = holdingService.readByUserIdAndStockCode(userId, tradeReqDto.stockCode())
                .orElseThrow(() -> new CustomException(HoldingErrorType.NOT_FOUND));

        if (!holding.increaseReservedStock(tradeReqDto.stockCount()))
            throw new CustomException(HoldingErrorType.INSUFFICIENT_STOCK);
    }

    /**
     * 예약 처리 완료 후 처리
     * 1. 예약 내역 db 저장
     * 2. 예약된 종목 코드 redis 저장 (종목 코드에 대한 주식 현재가 불러오기 위함)
     *
     * @param userId : 사용자 pk값
     * @param tradeReqDto : trade 정보 dto
     */
    private void finalizeSaveReservation(Long userId, TradeReqDto tradeReqDto) {
        TradeReservation reservation = TradeReservation.builder()
                .userId(userId)
                .inputPrice(tradeReqDto.inputPrice())
                .stockCount(tradeReqDto.stockCount())
                .stockName(tradeReqDto.stockName())
                .stockCode(tradeReqDto.stockCode())
                .tradeType(tradeReqDto.tradeType())
                .build();

        tradeReservationService.save(reservation);

        redisService.addToSet(TradeConstant.RESERVATION_PREFIX.getValue(), tradeReqDto.stockCode());
    }

    /**
     * 예약된 주식 거래를 취소하는 메서드
     *
     * @param userId : 사용자 pk값
     * @param tradeReservationId : 예약주식 pk값
     */
    public void cancelTradeReservation(Long userId, Long tradeReservationId) {
        TradeReservation reservation = tradeReservationService.readById(tradeReservationId)
                .orElseThrow(() -> new CustomException(TradeErrorType.NOT_FOUND));

        if (!reservation.getUserId().equals(userId)) {
            throw new CustomException(TradeErrorType.UNAUTHORIZED_ACCESS);
        }

        switch (reservation.getTradeType()) {
            case BUY -> cancelReservationPurchase(reservation, userId);
            case SELL -> cancelReservationSale(reservation, userId);
            default -> throw new IllegalArgumentException("잘못된 거래 유형입니다.");
        }

        finalizeCancelReservation(tradeReservationId, reservation.getStockCode());
    }

    /**
     * 예약된 매수 거래 취소
     * -> 예약된 금액을 계좌 잔액으로 되돌린다.
     */
    private void cancelReservationPurchase(TradeReservation reservation, Long userId) {
        Account account = accountService.readByUserId(userId)
                .orElseThrow(() -> new CustomException(AccountErrorType.NOT_FOUND));

        long price = reservation.getInputPrice() * reservation.getStockCount();
        account.decreaseReservedPrice(price);
    }

    /**
     * 예약된 매도 거래 취소
     * -> 예약된 주식 수량을 원래대로 복구한다.
     */
    private void cancelReservationSale(TradeReservation reservation, Long userId) {
        Holding holding = holdingService.readByUserIdAndStockCode(userId, reservation.getStockCode())
                .orElseThrow(() -> new CustomException(HoldingErrorType.NOT_FOUND));

        holding.decreaseReservedStock(reservation.getStockCount());
    }

    /**
     * 예약 취소 완료 후 처리
     * 1. 예약 내역 삭제
     * 2. 종목 코드에 예약 내역 없으면, redis 값 삭제
     *
     * @param tradeReservationId : 예약 내역 id
     * @param stockCode : 주식 종목 코드
     */
    private void finalizeCancelReservation(Long tradeReservationId, String stockCode) {
        tradeReservationService.deleteById(tradeReservationId);

        if (!tradeReservationService.existsByStockCode(stockCode)) {
            redisService.removeFromSet(TradeConstant.RESERVATION_PREFIX.getValue(), stockCode);
        }
    }
}
