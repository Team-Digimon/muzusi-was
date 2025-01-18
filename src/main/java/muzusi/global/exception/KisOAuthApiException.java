package muzusi.global.exception;

public class KisOAuthApiException extends RuntimeException {

    public KisOAuthApiException(Throwable exception) {
        super(exception.getMessage(), null, false, false);
    }
}
