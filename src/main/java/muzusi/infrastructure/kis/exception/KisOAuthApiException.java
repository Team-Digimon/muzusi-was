package muzusi.infrastructure.kis.exception;

public class KisOAuthApiException extends KisApiException {

    public KisOAuthApiException(Throwable cause) {
        super(cause);
    }
    
    public KisOAuthApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
