package muzusi.global.exception;

public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message) {
        super(message, null, false, false);
    }
    
    public ExternalApiException(Throwable cause, String message) {
        super(message, cause, false, false);
    }
}
