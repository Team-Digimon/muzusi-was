package muzusi.infrastructure.kis.stockranking.adapter;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.StockRankDto;
import muzusi.application.stockranking.port.FetchStockRankingPort;
import muzusi.infrastructure.kis.ranking.KisRankingClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KisFetchStockRankingAdapter implements FetchStockRankingPort {
    private final KisRankingClient kisRankingClient;
    
    @Override
    public List<StockRankDto> getVolumeRank() {
        return kisRankingClient.getVolumeRank();
    }
    
    @Override
    public List<StockRankDto> getRisingFluctuationRank() {
        return kisRankingClient.getRisingFluctuationRank();
    }
    
    @Override
    public List<StockRankDto> getFallingFluctuationRank() {
        return kisRankingClient.getFallingFluctuationRank();
    }
}
