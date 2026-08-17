package muzusi.infrastructure.kis.exception;

import muzusi.global.exception.ExternalApiException;

public class KisApiException extends ExternalApiException {
    public KisApiException(String message) {
        super(message);
    }
    
    public KisApiException(Throwable cause) {
        super(cause);
    }
    
    public KisApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
