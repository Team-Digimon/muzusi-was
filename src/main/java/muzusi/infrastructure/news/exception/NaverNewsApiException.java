package muzusi.infrastructure.news.exception;

import muzusi.global.exception.ExternalApiException;

public class NaverNewsApiException extends ExternalApiException {
    public NaverNewsApiException(String message) {
        super(message);
    }
    
    public NaverNewsApiException(Throwable cause) {
        super(cause);
    }
    
    public NaverNewsApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
