package muzusi.application.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SignUpDto", description = "회원가입 DTO")
public record SignUpDto(
        @Schema(description = "낙네임", example = "test")
        String nickname
) {
}
