package muzusi.application.stockranking.port;

import muzusi.application.stockranking.dto.StockRankDto;

import java.util.List;

public interface FetchStockRankingPort {
    List<StockRankDto> getVolumeRank();
    List<StockRankDto> getRisingFluctuationRank();
    List<StockRankDto> getFallingFluctuationRank();
}
