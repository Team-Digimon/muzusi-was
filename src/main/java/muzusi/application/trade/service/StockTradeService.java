package muzusi.application.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.trade.dto.TradeReqDto;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.exception.AccountErrorType;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.exception.HoldingErrorType;
import muzusi.domain.holding.service.HoldingService;
import muzusi.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockTradeService {
    private final StockTradeExecutor stockTradeExecutor;
    private final TradeReservationHandler tradeReservationHandler;
    private final AccountService accountService;
    private final HoldingService holdingService;

    /**
     * 주식 거래의 상태(매수/매도 가능 및 예약 처리)를 확인하는 메서드
     *
     * @param userId : 사용자 pk값
     * @param tradeReqDto : trade 정보 dto
     */
    @Transactional
    public void tradeStock(Long userId, TradeReqDto tradeReqDto) {
        switch (tradeReqDto.tradeType()) {
            case BUY -> processBuyTrade(userId, tradeReqDto);
            case SELL -> processSellTrade(userId, tradeReqDto);
            default -> throw new IllegalArgumentException("잘못된 거래 유형입니다.");
        }
    }

    /**
     * 주식을 사는 메서드.
     * 보유 잔액이 매수하고자 하는 금액보다 적다 -> 예외처리
     * 사용자 입력 값 >= 주식 가격 : 주식 거래 성사
     * 사용자 입력 값 < 주식 가격 : 주식 거래 예약
     */
    private void processBuyTrade(Long userId, TradeReqDto tradeReqDto) {
        Account account = accountService.readByUserId(userId)
                .orElseThrow(() -> new CustomException(AccountErrorType.NOT_FOUND));

        long requiredAmount = tradeReqDto.stockPrice() * tradeReqDto.stockCount();

        if (account.getBalance() < requiredAmount)
            throw new CustomException(AccountErrorType.INSUFFICIENT_BALANCE);

        if (tradeReqDto.inputPrice() >= tradeReqDto.stockPrice()) {
            stockTradeExecutor.executeTrade(userId, tradeReqDto);
        } else {
            tradeReservationHandler.saveTradeReservation(userId, tradeReqDto);
        }
    }

    /**
     * 주식을 파는 메서드.
     * 보유 주식 수량이 매도하고자 하는 개수보다 적다 -> 예외처리
     * 사용자 입력 값 <= 주식 가격 : 주식 거래 성사
     * 사용자 입력 값 > 주식 가격 : 주식 거래 예약
     */
    private void processSellTrade(Long userId, TradeReqDto tradeReqDto) {
        Holding holding = holdingService.readByStockCode(tradeReqDto.stockCode())
                .orElseThrow(() -> new CustomException(HoldingErrorType.NOT_FOUND));

        if (holding.getStockCount() < tradeReqDto.stockCount())
            throw new CustomException(HoldingErrorType.INSUFFICIENT_STOCK);

        if (tradeReqDto.inputPrice() <= tradeReqDto.stockPrice()) {
            stockTradeExecutor.executeTrade(userId, tradeReqDto);
        } else {
            tradeReservationHandler.saveTradeReservation(userId, tradeReqDto);
        }
    }

}
