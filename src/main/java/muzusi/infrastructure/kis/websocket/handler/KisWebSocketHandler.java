package muzusi.infrastructure.kis.websocket.handler;

import muzusi.infrastructure.kis.websocket.dto.KisWebSocketResponseDto;

public interface KisWebSocketHandler {
    String getTrId();
    void handle(KisWebSocketResponseDto response);
}
