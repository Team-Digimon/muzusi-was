package muzusi.infrastructure.kis.websocket;

import muzusi.infrastructure.kis.exception.KisApiException;
import muzusi.infrastructure.kis.websocket.handler.KisWebSocketDispatcher;
import muzusi.infrastructure.properties.KisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KisWebSocketConnectorTest {

    private static final String WEBSOCKET_DOMAIN = "wss://kis.example.com";

    @Mock
    private KisProperties kisProperties;

    @Mock
    private KisWebSocketDispatcher kisWebSocketDispatcher;

    @Mock
    private WebSocketClient webSocketClient;

    private KisWebSocketConnector kisWebSocketConnector;

    @BeforeEach
    void setUp() {
        when(kisProperties.getWebSocketDomain()).thenReturn(WEBSOCKET_DOMAIN);
        kisWebSocketConnector = new KisWebSocketConnector(kisProperties, kisWebSocketDispatcher);

        // webSocketClient는 생성자로 주입되지 않고 필드에서 직접 생성되므로,
        // 테스트에서 목(mock)으로 교체하기 위해 리플렉션을 사용한다.
        ReflectionTestUtils.setField(kisWebSocketConnector, "webSocketClient", webSocketClient);
    }

    @Nested
    @DisplayName("연결")
    class Connect {
        @Test
        @DisplayName("웹소켓 연결에 성공하면 연결된 세션을 반환한다")
        void successReturnConnectedSession() {
            // given
            WebSocketSession session = org.mockito.Mockito.mock(WebSocketSession.class);
            when(webSocketClient.execute(kisWebSocketDispatcher, WEBSOCKET_DOMAIN))
                    .thenReturn(CompletableFuture.completedFuture(session));

            // when
            WebSocketSession connectedSession = kisWebSocketConnector.connect();

            // then
            assertThat(connectedSession).isEqualTo(session);
            verify(webSocketClient).execute(kisWebSocketDispatcher, WEBSOCKET_DOMAIN);
        }

        @Test
        @DisplayName("웹소켓 클라이언트가 연결 시도 중 예외를 던지면 KisApiException을 던진다")
        void throwKisApiExceptionWhenExecuteThrows() {
            // given
            when(webSocketClient.execute(any(), anyString()))
                    .thenThrow(new IllegalStateException("invalid uri"));

            // when & then
            assertThatThrownBy(() -> kisWebSocketConnector.connect())
                    .isInstanceOf(KisApiException.class)
                    .hasMessage("한국투자증권 웹소켓 세션 연결에 실패하였습니다.")
                    .hasCauseInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("웹소켓 연결이 비동기적으로 실패하면 KisApiException을 던진다")
        void throwKisApiExceptionWhenConnectionFailsAsynchronously() {
            // given
            CompletableFuture<WebSocketSession> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("connection refused"));
            when(webSocketClient.execute(any(), anyString())).thenReturn(future);

            // when & then
            assertThatThrownBy(() -> kisWebSocketConnector.connect())
                    .isInstanceOf(KisApiException.class)
                    .hasMessage("한국투자증권 웹소켓 세션 연결에 실패하였습니다.");
        }
    }
}
