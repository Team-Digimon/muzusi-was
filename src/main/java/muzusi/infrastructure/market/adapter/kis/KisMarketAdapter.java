package muzusi.infrastructure.market.adapter.kis;

import lombok.RequiredArgsConstructor;
import muzusi.application.market.port.MarketPort;
import muzusi.infrastructure.market.client.kis.KisMarketOpenClient;
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
