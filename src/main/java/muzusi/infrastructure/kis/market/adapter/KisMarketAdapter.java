package muzusi.infrastructure.kis.market.adapter;

import lombok.RequiredArgsConstructor;
import muzusi.application.market.port.MarketPort;
import muzusi.infrastructure.kis.market.adapter.client.KisMarketOpenClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KisMarketAdapter implements MarketPort {
    private final KisMarketOpenClient kisMarketOpenClient;
    
    @Override
    public boolean isOpen() {
        return kisMarketOpenClient.isMarketOpen();
    }
}
