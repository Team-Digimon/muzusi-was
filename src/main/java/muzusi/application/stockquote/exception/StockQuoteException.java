package muzusi.application.stockquote.exception;

import lombok.Getter;
import muzusi.global.response.error.type.BaseErrorType;

@Getter
public class StockQuoteException extends RuntimeException {
    private final String stockCode;
    private final BaseErrorType errorType;
    
    public StockQuoteException(String stockCode, BaseErrorType errorType) {
        super(errorType.getMessage() + " - stockCode: " + stockCode);
        this.stockCode = stockCode;
        this.errorType = errorType;
    }
}
