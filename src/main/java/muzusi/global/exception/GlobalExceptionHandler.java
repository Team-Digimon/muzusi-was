package muzusi.global.exception;

import lombok.extern.slf4j.Slf4j;
import muzusi.global.response.error.ErrorResponse;
import muzusi.global.response.error.type.BaseErrorType;
import muzusi.global.response.error.type.CommonErrorType;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /* CustomException 예외 처리 */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<?> handleCustomException(final CustomException e) {
        BaseErrorType error = e.getErrorType();
        log.warn("[Error/Business] {} - {}", error.getCode(), error.getMessage());
        return ResponseEntity.status(error.getStatus()).body(ErrorResponse.from(error));
    }

    /* 외부 API 장애 처리 */
    @ExceptionHandler(ExternalApiException.class)
    protected ResponseEntity<?> handleExternalApiException(final ExternalApiException e) {
        log.error("[Error/External API] {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.from(CommonErrorType.EXTERNAL_API_ERROR));
    }

    /* 일반 예외 처리 */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleException(final Exception e) {
        log.error("[Error/Unexpected] {}", e.getMessage(), e);
        BaseErrorType error = CommonErrorType.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(error.getStatus()).body(ErrorResponse.from(error));
    }

    /* 요청 인자 유효성 검증 실패 처리 */
    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        
        log.warn("[Error/Validation] {}", errors);
        ErrorResponse responseBody = ErrorResponse.from(CommonErrorType.INVALID_REQUEST, errors);
        return handleExceptionInternal(ex, responseBody, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }
    
    /* 그 외 모든 Spring MVC 표준 예외 처리 */
    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        Object responseBody = body;
        if (body == null || body instanceof ProblemDetail) {
            log.warn("[Error/Spring MVC] {} - {}", statusCode, ex.getMessage());
            
            responseBody = ErrorResponse.from(CommonErrorType.INVALID_REQUEST, ex.getMessage());
        }

        return super.handleExceptionInternal(ex, responseBody, headers, statusCode, request);
    }
}
