package muzusi.application.stockcode.exception;

public class StockCodeException extends RuntimeException {
    public StockCodeException(String message) {
        super(message, null, false, false);
    }
    
    public StockCodeException(String message, Throwable cause) {
        super(message, cause, false, false);
    }
}
