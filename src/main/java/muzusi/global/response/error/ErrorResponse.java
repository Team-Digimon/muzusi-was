package muzusi.global.response.error;

import lombok.Builder;
import muzusi.global.response.error.type.BaseErrorType;

@Builder
public record ErrorResponse(
        int code,
        String message
) {
    public static ErrorResponse from(BaseErrorType error){
        return ErrorResponse.builder()
                .code(error.getCode())
                .message(error.getMessage())
                .build();
    }
}
