package muzusi.global.response.error.type;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * User ErrorCode: 1xxx
 */
@RequiredArgsConstructor
public enum UserErrorType implements BaseErrorType {

    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, 1001, "사용 기한이 만료된 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 1002, "사용 기한이 만료된 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 1003, "유효하지 않은 토큰입니다."),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, 1004, "형식이 잘못된 토큰입니다."),

    UN_AUTHORIZED(HttpStatus.FORBIDDEN, 1005, "허용되지 않은 접근입니다.")
    ;

    private final HttpStatus status;
    private final int code;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
