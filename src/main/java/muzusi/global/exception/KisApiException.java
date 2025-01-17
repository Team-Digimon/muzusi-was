package muzusi.global.exception;

public class KisApiException extends RuntimeException {

    public KisApiException(Throwable exception) {
        super(exception.getMessage(), null, false, false);
    }
}
