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
import muzusi.domain.trade.service.TradeReservationService;
import muzusi.global.exception.CustomException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeReservationHandler {
    private final TradeReservationService tradeReservationService;
    private final AccountService accountService;
    private final HoldingService holdingService;

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

        TradeReservation reservation = TradeReservation.builder()
                .userId(userId)
                .inputPrice(tradeReqDto.inputPrice())
                .stockCount(tradeReqDto.stockCount())
                .stockCode(tradeReqDto.stockCode())
                .tradeType(tradeReqDto.tradeType())
                .build();

        tradeReservationService.save(reservation);
    }

    /**
     * 매수 예약 처리 메서드
     * 계좌 잔액을 차감한다.
     */
    private void handleReservationPurchase(TradeReqDto tradeReqDto, Long userId) {
        Account account = accountService.readByUserId(userId)
                .orElseThrow(() -> new CustomException(AccountErrorType.NOT_FOUND));

        long price = tradeReqDto.inputPrice() * tradeReqDto.stockCount();

        account.updateAccount(tradeReqDto.tradeType(), price);
    }

    /**
     * 매도 예약 처리
     * 사용자가 보유한 주식 수량을 차감한다
     */
    private void handleReservationSale(TradeReqDto tradeReqDto, Long userId) {
        Holding holding = holdingService.readByUserIdAndStockCode(userId, tradeReqDto.stockCode())
                .orElseThrow(() -> new CustomException(HoldingErrorType.NOT_FOUND));

        if (!holding.reserveSellStock(tradeReqDto.stockCount()))
            throw new CustomException(HoldingErrorType.INSUFFICIENT_STOCK);
    }
}
