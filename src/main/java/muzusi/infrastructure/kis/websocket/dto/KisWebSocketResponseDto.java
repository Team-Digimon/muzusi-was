package muzusi.infrastructure.kis.websocket.dto;

import lombok.Builder;

@Builder
public record KisWebSocketResponseDto(
        boolean isEncoded,
        String trId,
        int dataCount,
        String data
) {
}
