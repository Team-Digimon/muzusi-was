package muzusi.infrastructure.stockquote.adapter.kis;

import muzusi.infrastructure.kis.auth.KisAuthStore;
import muzusi.infrastructure.kis.exception.KisApiException;
import muzusi.infrastructure.stockquote.requester.kis.KisStockQuoteRequester;
import muzusi.infrastructure.kis.websocket.KisWebSocketConnector;
import muzusi.infrastructure.kis.websocket.KisWebSocketSessionStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KisStockQuoteAdapterTest {

    @Mock
    private KisWebSocketConnector kisWebSocketConnector;

    @Mock
    private KisAuthStore kisAuthStore;

    @Mock
    private KisWebSocketSessionStore kisWebSocketSessionStore;

    @Mock
    private KisStockQuoteRequester kisStockQuoteRequester;

    @InjectMocks
    private KisStockQuoteAdapter kisStockQuoteAdapter;

    @Nested
    @DisplayName("연결")
    class Connect {
        @Test
        @DisplayName("발급받은 웹소켓 접속키 개수만큼 세션을 연결하고 저장한 뒤, 세션 ID 목록을 반환한다")
        void successConnectAllWebSocketKeys() {
            // given
            String webSocketKey1 = "webSocketKey1";
            String webSocketKey2 = "webSocketKey2";
            when(kisAuthStore.getWebSocketKeys()).thenReturn(List.of(webSocketKey1, webSocketKey2));

            WebSocketSession session1 = mock(WebSocketSession.class);
            WebSocketSession session2 = mock(WebSocketSession.class);
            when(kisWebSocketConnector.connect()).thenReturn(session1, session2);

            when(kisWebSocketSessionStore.save(session1, webSocketKey1)).thenReturn("sessionId1");
            when(kisWebSocketSessionStore.save(session2, webSocketKey2)).thenReturn("sessionId2");

            // when
            List<String> connectedSessionIds = kisStockQuoteAdapter.connect();

            // then
            assertThat(connectedSessionIds).containsExactly("sessionId1", "sessionId2");
            verify(kisWebSocketConnector, times(2)).connect();
            verify(kisWebSocketSessionStore).save(session1, webSocketKey1);
            verify(kisWebSocketSessionStore).save(session2, webSocketKey2);
        }

        @Test
        @DisplayName("발급받은 웹소켓 접속키가 없으면 연결을 시도하지 않고 빈 목록을 반환한다")
        void successReturnEmptyListWhenNoWebSocketKeys() {
            // given
            when(kisAuthStore.getWebSocketKeys()).thenReturn(List.of());

            // when
            List<String> connectedSessionIds = kisStockQuoteAdapter.connect();

            // then
            assertThat(connectedSessionIds).isEmpty();
            verify(kisWebSocketConnector, never()).connect();
            verifyNoInteractions(kisWebSocketSessionStore);
        }
    }

    @Nested
    @DisplayName("연결 해제")
    class Disconnect {
        @Test
        @DisplayName("저장된 세션이 없으면 세션 종료 없이 저장소만 비운다")
        void successDeleteAllSessionsWhenNoSessions() {
            // given
            when(kisWebSocketSessionStore.findAll()).thenReturn(List.of());

            // when
            kisStockQuoteAdapter.disconnect();

            // then
            verify(kisWebSocketConnector, never()).close(any());
            verify(kisWebSocketSessionStore).deleteAll();
        }

        @Test
        @DisplayName("저장된 모든 세션에 대해 웹소켓 연결을 종료한 뒤 저장소를 비운다")
        void successCloseAllSessionsThenDeleteAll() {
            // given
            WebSocketSession session1 = mock(WebSocketSession.class);
            WebSocketSession session2 = mock(WebSocketSession.class);
            KisWebSocketSessionStore.KisWebSocketSession kisWebSocketSession1 =
                    new KisWebSocketSessionStore.KisWebSocketSession("webSocketKey1", session1);
            KisWebSocketSessionStore.KisWebSocketSession kisWebSocketSession2 =
                    new KisWebSocketSessionStore.KisWebSocketSession("webSocketKey2", session2);
            when(kisWebSocketSessionStore.findAll()).thenReturn(List.of(kisWebSocketSession1, kisWebSocketSession2));

            // when
            kisStockQuoteAdapter.disconnect();

            // then
            verify(kisWebSocketConnector).close(session1);
            verify(kisWebSocketConnector).close(session2);
            verify(kisWebSocketSessionStore).deleteAll();
        }

        @Test
        @DisplayName("일부 세션 종료에 실패해도 나머지 세션은 계속 종료를 시도하고 저장소는 비운다")
        void successContinueClosingRemainingSessionsWhenOneFails() {
            // given
            WebSocketSession session1 = mock(WebSocketSession.class);
            WebSocketSession session2 = mock(WebSocketSession.class);
            KisWebSocketSessionStore.KisWebSocketSession kisWebSocketSession1 =
                    new KisWebSocketSessionStore.KisWebSocketSession("webSocketKey1", session1);
            KisWebSocketSessionStore.KisWebSocketSession kisWebSocketSession2 =
                    new KisWebSocketSessionStore.KisWebSocketSession("webSocketKey2", session2);
            when(kisWebSocketSessionStore.findAll()).thenReturn(List.of(kisWebSocketSession1, kisWebSocketSession2));
            doThrow(new KisApiException("세션 종료 실패")).when(kisWebSocketConnector).close(session1);

            // when
            kisStockQuoteAdapter.disconnect();

            // then: session1 종료가 실패해도 session2 종료는 정상적으로 시도되고, 저장소는 비워진다.
            verify(kisWebSocketConnector).close(session1);
            verify(kisWebSocketConnector).close(session2);
            verify(kisWebSocketSessionStore).deleteAll();
        }
    }

    @Nested
    @DisplayName("구독")
    class Subscribe {
        @Test
        @DisplayName("세션이 존재하면 해당 세션의 웹소켓 접속키로 구독을 요청한다")
        void successRequestSubscribe() {
            // given
            String sessionId = "sessionId1";
            String webSocketKey = "webSocketKey1";
            String stockCode = "000001";
            WebSocketSession session = mock(WebSocketSession.class);
            KisWebSocketSessionStore.KisWebSocketSession kisWebSocketSession =
                    new KisWebSocketSessionStore.KisWebSocketSession(webSocketKey, session);
            when(kisWebSocketSessionStore.findBySessionId(sessionId)).thenReturn(kisWebSocketSession);

            // when
            kisStockQuoteAdapter.subscribe(sessionId, stockCode);

            // then
            verify(kisStockQuoteRequester).subscribe(session, webSocketKey, stockCode);
        }

        @Test
        @DisplayName("세션이 존재하지 않으면 구독을 요청하지 않는다")
        void doNothingWhenSessionNotFound() {
            // given
            String sessionId = "unknownSessionId";
            String stockCode = "000001";
            when(kisWebSocketSessionStore.findBySessionId(sessionId)).thenReturn(null);

            // when
            kisStockQuoteAdapter.subscribe(sessionId, stockCode);

            // then
            verifyNoInteractions(kisStockQuoteRequester);
        }
    }

    @Nested
    @DisplayName("구독 해제")
    class Unsubscribe {
        @Test
        @DisplayName("세션이 존재하면 해당 세션의 웹소켓 접속키로 구독 해제를 요청한다")
        void successRequestUnsubscribe() {
            // given
            String sessionId = "sessionId1";
            String webSocketKey = "webSocketKey1";
            String stockCode = "000001";
            WebSocketSession session = mock(WebSocketSession.class);
            KisWebSocketSessionStore.KisWebSocketSession kisWebSocketSession =
                    new KisWebSocketSessionStore.KisWebSocketSession(webSocketKey, session);
            when(kisWebSocketSessionStore.findBySessionId(sessionId)).thenReturn(kisWebSocketSession);

            // when
            kisStockQuoteAdapter.unsubscribe(sessionId, stockCode);

            // then
            verify(kisStockQuoteRequester).unsubscribe(session, webSocketKey, stockCode);
        }

        @Test
        @DisplayName("세션이 존재하지 않으면 구독 해제를 요청하지 않는다")
        void doNothingWhenSessionNotFound() {
            // given
            String sessionId = "unknownSessionId";
            String stockCode = "000001";
            when(kisWebSocketSessionStore.findBySessionId(sessionId)).thenReturn(null);

            // when
            kisStockQuoteAdapter.unsubscribe(sessionId, stockCode);

            // then
            verifyNoInteractions(kisStockQuoteRequester);
        }
    }
}
