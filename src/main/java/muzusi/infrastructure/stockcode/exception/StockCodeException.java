package muzusi.infrastructure.stockcode.exception;

public class StockCodeException extends RuntimeException {
    public StockCodeException(String message) {
        super(message);
    }
    
    public StockCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
