package muzusi.infrastructure;

import muzusi.infrastructure.kis.auth.KisAuthService;
import muzusi.infrastructure.kis.websocket.KisWebSocketConnector;
import muzusi.infrastructure.kis.websocket.KisWebSocketSessionManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KisWebSocketSessionManagerTest {

    @Mock
    private KisWebSocketConnector kisWebSocketConnector;
    
    @Mock
    private KisAuthService kisAuthService;
    
    @InjectMocks
    private KisWebSocketSessionManager kisWebSocketSessionManager;
    
    @Test
    @DisplayName("세션 초기화 성공")
    void successInitializeSessions() {
        // given
        String webSocketKey1 = "webSocketKey1";
        String webSocketKey2 = "webSocketKey2";
        WebSocketSession session1 = mock(WebSocketSession.class);
        WebSocketSession session2 = mock(WebSocketSession.class);
        when(session1.getId()).thenReturn("session1");
        when(session2.getId()).thenReturn("session2");
        
        when(kisAuthService.getWebSocketKeys()).thenReturn(List.of(webSocketKey1, webSocketKey2));
        when(kisWebSocketConnector.connect()).thenReturn(session1).thenReturn(session2);
        
        // when
        List<String> sessionIds = kisWebSocketSessionManager.initializeSessions();
        
        // then
        assertThat(sessionIds).containsExactlyInAnyOrder("session1", "session2");
        assertNotNull(kisWebSocketSessionManager.getKisWebSocketSession("session1"));
        assertNotNull(kisWebSocketSessionManager.getKisWebSocketSession("session2"));
    }
    
    @Test
    @DisplayName("세션 초기화 실패")
    void failInitializeSessions() {
        // given
        String webSocketKey1 = "webSocketKey1";
        String webSocketKey2 = "webSocketKey2";
        when(kisAuthService.getWebSocketKeys()).thenReturn(List.of(webSocketKey1, webSocketKey2));
        when(kisWebSocketConnector.connect()).thenReturn(null);
        
        // when
        List<String> sessionIds = kisWebSocketSessionManager.initializeSessions();
        
        // then
        assertThat(sessionIds).isEmpty();
        Map<String, KisWebSocketSessionManager.KisWebSocketSession> sessions = (Map<String, KisWebSocketSessionManager.KisWebSocketSession>) ReflectionTestUtils.getField(kisWebSocketSessionManager, "sessions");
        assertThat(sessions.isEmpty());
    }
    
    @Test
    @DisplayName("세션 종료 및 삭제 성공")
    void successCloseSessions() throws IOException {
        // given
        WebSocketSession session1 = mock(WebSocketSession.class);
        WebSocketSession session2 = mock(WebSocketSession.class);
        
        Map<String, KisWebSocketSessionManager.KisWebSocketSession> sessions = new HashMap<>();
        
        sessions.put("session1", new KisWebSocketSessionManager.KisWebSocketSession(session1, "webSocketKey1"));
        sessions.put("session2", new KisWebSocketSessionManager.KisWebSocketSession(session2, "webSocketKey2"));
        
        ReflectionTestUtils.setField(kisWebSocketSessionManager, "sessions", sessions);
        
        // when
        kisWebSocketSessionManager.closeSessions();
        
        // then
        verify(session1, times(1)).close();
        verify(session2, times(1)).close();
        assertThat(sessions).isEmpty();
    }
}
