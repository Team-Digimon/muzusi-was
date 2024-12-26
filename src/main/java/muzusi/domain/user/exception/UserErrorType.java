package muzusi.domain.user.exception;

import lombok.RequiredArgsConstructor;
import muzusi.global.response.error.type.BaseErrorType;
import org.springframework.http.HttpStatus;

/**
 * User ErrorCode: 1xxx
 */
@RequiredArgsConstructor
public enum UserErrorType implements BaseErrorType {

    UN_AUTHORIZED(HttpStatus.FORBIDDEN, "1005", "허용되지 않은 접근입니다."),
    UNSUPPORTED_SOCIAL_LOGIN(HttpStatus.BAD_REQUEST, "1006", "지원하지 않는 소셜 로그인입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "1007", "존재하지 않는 사용자입니다.")
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
