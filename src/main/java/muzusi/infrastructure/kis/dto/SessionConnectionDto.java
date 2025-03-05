package muzusi.infrastructure.kis.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

@Builder
@Getter
public class SessionConnectionDto {
    private WebSocketSession session;
    private String webSocketKey;

    public static SessionConnectionDto of(WebSocketSession session, String webSocketKey) {
        return SessionConnectionDto.builder()
                .session(session)
                .webSocketKey(webSocketKey)
                .build();
    }
}