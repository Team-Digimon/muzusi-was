package muzusi.global.exception;

public class ExternalApiRateLimitExceededException extends ExternalApiException {
    public ExternalApiRateLimitExceededException(String message) {
        super(message);
    }
}
