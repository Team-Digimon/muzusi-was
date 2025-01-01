package muzusi.domain.news.exception;

import lombok.RequiredArgsConstructor;
import muzusi.global.response.error.type.BaseErrorType;
import org.springframework.http.HttpStatus;

/**
 * News ErrorCode: 3xxx
 */
@RequiredArgsConstructor
public enum NewsErrorType implements BaseErrorType {
    INVALID_KEYWORD(HttpStatus.BAD_REQUEST, "3001", "키워드가 올바르지 않습니다.");
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
