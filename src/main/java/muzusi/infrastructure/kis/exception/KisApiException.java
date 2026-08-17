package muzusi.infrastructure.kis.exception;

import muzusi.global.exception.ExternalApiException;

public class KisApiException extends ExternalApiException {
    public KisApiException(Throwable cause) {
        super(cause);
    }
}
