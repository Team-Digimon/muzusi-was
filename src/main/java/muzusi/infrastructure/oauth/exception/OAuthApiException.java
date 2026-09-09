package muzusi.infrastructure.oauth.exception;

import muzusi.global.exception.ExternalApiException;

public class OAuthApiException extends ExternalApiException {
    public OAuthApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
