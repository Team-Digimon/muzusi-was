package muzusi.application.market.port;

public interface MarketDataAuthPort {
    boolean isApiCredentialsExists();
    void issueApiCredentials();
    void issueWebSocketCredentials();
}
