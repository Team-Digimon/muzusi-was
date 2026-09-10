package muzusi.global.response.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import muzusi.global.response.error.type.BaseErrorType;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,
        Object data
) {
    public static ErrorResponse from(BaseErrorType error){
        return ErrorResponse.builder()
                .code(error.getCode())
                .message(error.getMessage())
                .build();
    }
    
    public static ErrorResponse from(BaseErrorType error, Object data){
        return ErrorResponse.builder()
                .code(error.getCode())
                .message(error.getMessage())
                .data(data)
                .build();
    }
}
