package muzusi.domain.stock.exception;

import lombok.RequiredArgsConstructor;
import muzusi.global.response.error.type.BaseErrorType;
import org.springframework.http.HttpStatus;

/**
 * Stock ErrorCode: 2xxx
 */
@RequiredArgsConstructor
public enum StockErrorType implements BaseErrorType {
    NOT_FOUND(HttpStatus.NOT_FOUND, "2001", "주식 종목이 존재하지 않습니다.")
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
