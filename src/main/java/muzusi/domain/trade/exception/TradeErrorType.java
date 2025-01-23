package muzusi.domain.trade.exception;

import lombok.RequiredArgsConstructor;
import muzusi.global.response.error.type.BaseErrorType;
import org.springframework.http.HttpStatus;

/**
 * Trade ErrorCode: 6xxx
 */
@RequiredArgsConstructor
public enum TradeErrorType implements BaseErrorType {
    NOT_FOUND(HttpStatus.NOT_FOUND, "5001", "예약 내역이 없습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "6002", "해당 예약을 취소할 권한이 없습니다.")
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
