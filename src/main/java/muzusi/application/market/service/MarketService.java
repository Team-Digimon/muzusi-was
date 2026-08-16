package muzusi.application.market.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.market.port.MarketPort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarketService {
    private final MarketPort marketPort;
    
    public boolean isMarketOpen() {
        return marketPort.isOpen();
    }
}
