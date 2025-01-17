package muzusi.global.exception;

public class NewsApiException extends RuntimeException {

    public NewsApiException(Throwable exception) {
        super(exception.getMessage(), null, false, false);
    }
}
