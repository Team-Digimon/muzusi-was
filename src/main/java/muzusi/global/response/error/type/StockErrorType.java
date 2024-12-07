package muzusi.global.response.error.type;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Stock ErrorCode: 2xxx
 */
@RequiredArgsConstructor
public enum StockErrorType implements BaseErrorType {
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
