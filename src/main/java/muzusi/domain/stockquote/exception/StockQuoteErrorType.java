package muzusi.domain.stockquote.exception;

import lombok.RequiredArgsConstructor;
import muzusi.global.response.error.type.BaseErrorType;
import org.springframework.http.HttpStatus;

/**
 * StockQuote ErrorCode: 21xx
 */
@RequiredArgsConstructor
public enum StockQuoteErrorType implements BaseErrorType {
    FAIL_SUBSCRIPTION(HttpStatus.BAD_REQUEST, "2100", "해당 주식 종목 구독에 실패하였습니다."),
    FAIL_UNSUBSCRIPTION(HttpStatus.BAD_REQUEST, "2101", "해당 주식 종목 구독 해제에 실패하였습니다."),
    ;
    
    private final HttpStatus status;
    private final String code;
    private final String message;
    
    @Override
    public HttpStatus getStatus() {
        return status;
    }
    
    @Override
    public String getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
