package muzusi.application.stock.dto;

import muzusi.domain.stock.entity.Stock;
import muzusi.domain.stock.type.MarketType;

public record StockResponse(
        String stockCode,
        String stockName,
        MarketType marketType
) {
    public static StockResponse from(Stock stock) {
        return new StockResponse(stock.getStockCode(), stock.getStockName(), stock.getMarketType());
    }
}
