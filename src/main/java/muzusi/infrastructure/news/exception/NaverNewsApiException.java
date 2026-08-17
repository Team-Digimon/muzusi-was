package muzusi.infrastructure.news.exception;

import muzusi.global.exception.ExternalApiException;

public class NaverNewsApiException extends ExternalApiException {
    public NaverNewsApiException(Throwable cause) {
        super(cause);
    }
}
