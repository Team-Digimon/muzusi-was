package muzusi.application.holding.dto;

import muzusi.domain.holding.entity.Holding;

import java.time.LocalDateTime;

public record HoldingInfoDto(
        Long id,
        String stockName,
        String stockCode,
        Integer stockCount,
        Long averagePrice,
        LocalDateTime holdingAt,
        Double rateOfReturn,
        Long totalProfitAmount
) {
    public static HoldingInfoDto from(Holding holding, Double rateOfReturn, Long totalProfitAmount) {
        return new HoldingInfoDto(
                holding.getId(), holding.getStockName(), holding.getStockCode(),
                holding.getStockCount(), holding.getAveragePrice(), holding.getHoldingAt(),
                rateOfReturn, totalProfitAmount
        );
    }
}
