package muzusi.application.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.trade.dto.TradeReqDto;
import muzusi.domain.trade.entity.TradeReservation;
import muzusi.domain.trade.service.TradeReservationService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeReservationHandler {
    private final TradeReservationService tradeReservationService;

    /**
     * 성사가 안된 주식 거래를 예약 처리하는 메서드
     *
     * @param userId : 사용자 pk값
     * @param tradeReqDto : trade 정보 dto
     */
    public void saveTradeReservation(Long userId, TradeReqDto tradeReqDto) {
        TradeReservation reservation = TradeReservation.builder()
                .userId(userId)
                .inputPrice(tradeReqDto.inputPrice())
                .stockCount(tradeReqDto.stockCount())
                .stockCode(tradeReqDto.stockCode())
                .tradeType(tradeReqDto.tradeType())
                .build();

        tradeReservationService.save(reservation);
    }
}
