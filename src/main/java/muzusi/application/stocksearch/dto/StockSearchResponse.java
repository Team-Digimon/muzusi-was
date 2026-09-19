package muzusi.application.stocksearch.dto;

public record StockSearchResponse (
        String stockCode,
        String stockName
) {
    public static StockSearchResponse from(StockSearchCandidate candidate) {
        return new StockSearchResponse(candidate.stockCode(), candidate.stockName());
    }
}
