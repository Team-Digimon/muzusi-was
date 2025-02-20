package muzusi.application.trade.dto;

import lombok.Builder;
import muzusi.domain.trade.type.TradeType;

@Builder
public record TradeNotificationDto(
    String stockCode,
    String time,
    Long price,
    Long stockCount,
    Long volume,
    TradeType tradeType,
    Double changeRate
) {
}