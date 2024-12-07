package muzusi.global.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import muzusi.global.response.error.type.BaseErrorType;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException{
    private final BaseErrorType errorType;
}
