package muzusi.application.stockquote.port;

import java.util.List;

public interface StockQuotePort {
    List<String> connect();
    void disconnect();
    void subscribe(String sessionId, String stockCode);
    void unsubscribe(String sessionId, String stockCode);
}
