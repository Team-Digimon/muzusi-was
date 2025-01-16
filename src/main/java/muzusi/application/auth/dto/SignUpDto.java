package muzusi.application.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(name = "SignUpDto", description = "회원가입 DTO")
public record SignUpDto(
        @Schema(description = "낙네임", example = "test")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,8}$", message = "2~8자의 한글, 영문, 숫자(공백, 특수문자 제외)")
        String nickname
) {
}
