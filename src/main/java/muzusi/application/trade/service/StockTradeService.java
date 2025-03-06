package muzusi.application.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.trade.dto.TradeReqDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockTradeService {
    private final StockTradeExecutor stockTradeExecutor;
    private final TradeReservationHandler tradeReservationHandler;

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
     * 사용자 입력 값 >= 주식 가격 : 주식 거래 성사
     * 사용자 입력 값 < 주식 가격 : 주식 거래 예약
     */
    private void processBuyTrade(Long userId, TradeReqDto tradeReqDto) {
        if (tradeReqDto.inputPrice() >= tradeReqDto.stockPrice()) {
            stockTradeExecutor.executeTrade(userId, tradeReqDto);
        } else {
            tradeReservationHandler.saveTradeReservation(userId, tradeReqDto);
        }
    }

    /**
     * 주식을 파는 메서드.
     * 사용자 입력 값 <= 주식 가격 : 주식 거래 성사
     * 사용자 입력 값 > 주식 가격 : 주식 거래 예약
     */
    private void processSellTrade(Long userId, TradeReqDto tradeReqDto) {
        if (tradeReqDto.inputPrice() <= tradeReqDto.stockPrice()) {
            stockTradeExecutor.executeTrade(userId, tradeReqDto);
        } else {
            tradeReservationHandler.saveTradeReservation(userId, tradeReqDto);
        }
    }

    /**
     * 예약 매수/매도 처리하는 메서드
     *
     * @param userId : 사용자 pk값
     * @param tradeReservationId : 예약주식 pk값
     */
    @Transactional
    public void cancelTradeReservation(Long userId, Long tradeReservationId) {
        tradeReservationHandler.cancelTradeReservation(userId, tradeReservationId);
    }
}
