package muzusi.infrastructure.kis.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KisWebSocketSessionStoreTest {
    private KisWebSocketSessionStore store;

    @BeforeEach
    void setUp() {
        store = new KisWebSocketSessionStore();
    }

    @Nested
    @DisplayName("저장")
    class Save {
        @Test
        @DisplayName("세션과 웹소켓 접속키를 저장하고 세션 ID를 반환한다")
        void successSaveAndReturnSessionId() {
            // given
            String sessionId = "sessionId1";
            String webSocketKey = "webSocketKey1";
            WebSocketSession session = mock(WebSocketSession.class);
            when(session.getId()).thenReturn(sessionId);

            // when
            String savedSessionId = store.save(session, webSocketKey);

            // then
            assertThat(savedSessionId).isEqualTo(sessionId);

            KisWebSocketSessionStore.KisWebSocketSession found = store.findBySessionId(sessionId);
            assertThat(found.getWebSocketSession()).isEqualTo(session);
            assertThat(found.getWebSocketKey()).isEqualTo(webSocketKey);
        }
    }

    @Nested
    @DisplayName("조회")
    class FindBySessionId {
        @Test
        @DisplayName("저장되지 않은 세션 ID로 조회하면 null을 반환한다")
        void returnNullWhenSessionNotFound() {
            // when
            KisWebSocketSessionStore.KisWebSocketSession found = store.findBySessionId("unknownSessionId");

            // then
            assertThat(found).isNull();
        }
    }

    @Nested
    @DisplayName("전체 삭제")
    class DeleteAll {
        @Test
        @DisplayName("저장된 모든 세션을 삭제한다")
        void successDeleteAllSessions() {
            // given
            WebSocketSession session1 = mock(WebSocketSession.class);
            WebSocketSession session2 = mock(WebSocketSession.class);
            when(session1.getId()).thenReturn("sessionId1");
            when(session2.getId()).thenReturn("sessionId2");
            store.save(session1, "webSocketKey1");
            store.save(session2, "webSocketKey2");

            // when
            store.deleteAll();

            // then
            assertThat(store.findBySessionId("sessionId1")).isNull();
            assertThat(store.findBySessionId("sessionId2")).isNull();
        }
    }
}
