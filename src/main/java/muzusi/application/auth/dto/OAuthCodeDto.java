package muzusi.application.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record OAuthCodeDto(
        String code
) {
}