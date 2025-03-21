package muzusi.global.response.error.type;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Common ErrorCode: 0xxx
 */
@RequiredArgsConstructor
public enum CommonErrorType implements BaseErrorType {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "0001", "요청을 실패하였습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "0002", "조회에 실패하였습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "0003", "서버 내부 에러입니다. 관리자에게 문의하세요."),

    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "0004", "Access Token이 만료되었습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "0005", "Access Token이 잘못되었습니다."),
    INVALID_TOKEN_SIGNATURE(HttpStatus.UNAUTHORIZED, "0006", "Access Token의 서명이 잘못되었습니다."),
    UNKNOWN_TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "0007", "알 수 없는 토큰 에러입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "0008", "Refresh Token이 존재하지 않습니다.")
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
