package muzusi.application.trade.dto;

import muzusi.domain.trade.entity.TradeReservation;
import muzusi.domain.trade.type.TradeType;

import java.time.LocalDateTime;

public record ReservationInfoDto(
        Long id,
        Long userId,
        Long inputPrice,
        Integer stockCount,
        String stockName,
        String stockCode,
        TradeType tradeType,
        LocalDateTime createdAt
) {
    public static ReservationInfoDto fromEntity(TradeReservation tradeReservation) {
        return new ReservationInfoDto(
                tradeReservation.getId(), tradeReservation.getUserId(),
                tradeReservation.getInputPrice(), tradeReservation.getStockCount(),
                tradeReservation.getStockName(), tradeReservation.getStockCode(),
                tradeReservation.getTradeType(), tradeReservation.getCreatedAt()
        );
    }
}
