package muzusi.infrastructure.kis.exception;

public class KisApiRateLimitExceedException extends KisApiException {
    public KisApiRateLimitExceedException(String message) {
        super(message);
    }
}
