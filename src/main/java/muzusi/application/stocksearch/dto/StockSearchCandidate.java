package muzusi.application.stocksearch.dto;

public record StockSearchCandidate(
        String stockCode,
        String stockName,
        double score
) {
    public StockSearchCandidate withScore(double score) {
        return new StockSearchCandidate(stockCode, stockName, score);
    }
}