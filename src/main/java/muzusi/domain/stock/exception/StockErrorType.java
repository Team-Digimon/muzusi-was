package muzusi.domain.stock.exception;

import lombok.RequiredArgsConstructor;
import muzusi.global.response.error.type.BaseErrorType;
import org.springframework.http.HttpStatus;

/**
 * Stock ErrorCode: 2xxx
 */
@RequiredArgsConstructor
public enum StockErrorType implements BaseErrorType {
    NOT_FOUND(HttpStatus.NOT_FOUND, "2001", "주식 종목이 존재하지 않습니다."),
    NOT_PREPARED(HttpStatus.NOT_FOUND, "2002", "아직 준비 중인 주식 종목입니다."),
    NOT_AVAILABLE_MINUTES_CHART(HttpStatus.BAD_REQUEST, "2003", "당일 주식 조회 가능 기간이 아닙니다."),
    UNSUPPORTED_MINUTES_PERIOD(HttpStatus.BAD_REQUEST, "2004", "주식 분봉 데이터 조회 시간이 아닙니다."),
    MAX_REQUEST_WEB_SOCKET(HttpStatus.BAD_REQUEST, "2005", "한국투자증권 웹소켓 호출 유량 초과"),
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
