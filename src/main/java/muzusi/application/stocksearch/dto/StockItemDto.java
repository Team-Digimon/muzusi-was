package muzusi.application.stocksearch.dto;

import muzusi.domain.stock.entity.StockItem;

public record StockItemDto(
        String stockName,
        String stockCode
) {
    public static StockItemDto fromEntity(StockItem stockItem) {
        return new StockItemDto(stockItem.getStockName(), stockItem.getStockCode());
    }
}
