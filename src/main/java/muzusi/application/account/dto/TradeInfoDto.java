package muzusi.application.account.dto;

import muzusi.domain.trade.entity.Trade;
import muzusi.domain.trade.type.TradeType;

import java.time.LocalDateTime;

public record TradeInfoDto(
        Long id,
        Long stockPrice,
        Integer stockCount,
        String stockName,
        String stockCode,
        TradeType tradeType,
        LocalDateTime tradeAt
) {
    public static TradeInfoDto fromEntity(Trade trade) {
        return new TradeInfoDto(
                trade.getId(), trade.getStockPrice(),
                trade.getStockCount(), trade.getStockName(),
                trade.getStockCode(), trade.getTradeType(), trade.getTradeAt()
        );
    }
}
