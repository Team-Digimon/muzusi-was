package muzusi.domain.holding.exception;

import lombok.RequiredArgsConstructor;
import muzusi.global.response.error.type.BaseErrorType;
import org.springframework.http.HttpStatus;

/**
 * Holding ErrorCode: 5xxx
 */
@RequiredArgsConstructor
public enum HoldingErrorType implements BaseErrorType {
    NOT_FOUND(HttpStatus.NOT_FOUND, "5001", "매수내역이 없습니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "5002", "잔여 주식이 부족합니다.")
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
