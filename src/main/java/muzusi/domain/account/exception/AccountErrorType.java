package muzusi.domain.account.exception;

import lombok.RequiredArgsConstructor;
import muzusi.global.response.error.type.BaseErrorType;
import org.springframework.http.HttpStatus;

/**
 * Account ErrorCode: 4xxx
 */
@RequiredArgsConstructor
public enum AccountErrorType implements BaseErrorType {
    NOT_FOUND(HttpStatus.NOT_FOUND, "4001", "계좌가 존재하지 않습니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "4002", "계좌 잔액이 부족합니다."),
    ACCOUNT_CREATION_LIMIT(HttpStatus.BAD_REQUEST, "4003", "오늘은 이미 계좌를 생성했습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "4004", "해당 계좌에 접근 권한이 없습니다.")
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
