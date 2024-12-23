package muzusi.global.response.error.type;

import org.springframework.http.HttpStatus;

public interface BaseErrorType {
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
