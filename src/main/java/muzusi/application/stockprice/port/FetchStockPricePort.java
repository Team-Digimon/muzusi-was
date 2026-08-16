package muzusi.application.stockprice.port;

import java.util.List;
import java.util.Map;

public interface FetchStockPricePort {
    Map<String, Long> getStockPrice(List<String> stockCodes);
}
