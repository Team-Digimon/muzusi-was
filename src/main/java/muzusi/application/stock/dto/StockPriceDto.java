package muzusi.application.stock.dto;

import lombok.Builder;

@Builder
public record StockPriceDto(
        String stockCode,
        long price
) {
}