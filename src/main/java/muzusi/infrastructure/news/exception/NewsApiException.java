package muzusi.infrastructure.news.exception;

import muzusi.global.exception.ExternalApiException;

public class NewsApiException extends ExternalApiException {
    public NewsApiException(Throwable cause) {
        super(cause);
    }
}
