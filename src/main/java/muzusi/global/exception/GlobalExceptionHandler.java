package muzusi.global.exception;

import lombok.extern.slf4j.Slf4j;
import muzusi.global.response.error.ErrorResponse;
import muzusi.global.response.error.type.BaseErrorType;
import muzusi.global.response.error.type.CommonErrorType;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* CustomException 예외 처리 */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<?> handleCustomException(final CustomException e) {
        BaseErrorType error = e.getErrorType();
        log.error("[Error Occurred] {}", e.getMessage());
        return ResponseEntity.status(error.getStatus()).body(ErrorResponse.from(error));
    }

    /* Argument Validation 예외 처리 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Map<String, String>> handleValidationException(final MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for(FieldError fieldError : e.getBindingResult().getFieldErrors() ){
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.error("[Error occurred] {}", errors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errors);
    }

    /* 일반 예외 처리 */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleException(final Exception e) {
        log.error("[Error Occurred] {}", e.getMessage());
        BaseErrorType error = CommonErrorType.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(error.getStatus()).body(ErrorResponse.from(error));
    }

}
