package muzusi.infrastructure.kis.stockranking.adapter;

import lombok.RequiredArgsConstructor;
import muzusi.application.stockranking.dto.StockRankDto;
import muzusi.application.stockranking.port.FetchStockRankingPort;
import muzusi.infrastructure.kis.stockranking.client.KisStockRankingClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KisFetchStockRankingAdapter implements FetchStockRankingPort {
    private final KisStockRankingClient kisStockRankingClient;
    
    @Override
    public List<StockRankDto> getVolumeRank() {
        return kisStockRankingClient.getVolumeRank();
    }
    
    @Override
    public List<StockRankDto> getRisingFluctuationRank() {
        return kisStockRankingClient.getRisingFluctuationRank();
    }
    
    @Override
    public List<StockRankDto> getFallingFluctuationRank() {
        return kisStockRankingClient.getFallingFluctuationRank();
    }
}
