package muzusi.global.response.error.type;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Common ErrorCode: 0xxx
 */
@RequiredArgsConstructor
public enum CommonErrorType implements BaseErrorType {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, 0001, "요청을 실패하였습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, 0002, "조회에 실패하였습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 0003, "서버 내부 에러입니다. 관리자에게 문의하세요."),
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
