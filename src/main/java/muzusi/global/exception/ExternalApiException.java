package muzusi.global.exception;

public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message) {
        super(message);
    }
    
    public ExternalApiException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
    
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
