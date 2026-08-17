package muzusi.global.exception;

public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message) {
        super(message, null, false, false);
    }
    
    public ExternalApiException(Throwable cause) {
        super(cause.getMessage(), cause, false, false);
    }
    
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause, false, false);
    }
}
