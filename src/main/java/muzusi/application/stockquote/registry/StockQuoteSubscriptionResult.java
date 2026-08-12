package muzusi.application.stockquote.registry;

public class StockQuoteSubscriptionResult {
    public record Subscription(
            String sessionId,
            boolean isSuccess,
            boolean isNewSubscription
    ) { }
    
    public record Unsubscription(
            String sessionId,
            boolean isSuccess,
            boolean isDeleted
    ) { }
}
