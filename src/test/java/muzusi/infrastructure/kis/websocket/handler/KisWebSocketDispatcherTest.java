package muzusi.infrastructure.kis.websocket.handler;

import muzusi.infrastructure.kis.websocket.KisWebSocketMessageParser;
import muzusi.infrastructure.kis.websocket.dto.KisWebSocketMetaResponseDto;
import muzusi.infrastructure.kis.websocket.dto.KisWebSocketResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KisWebSocketDispatcherTest {

    private static final String TR_ID = "H0STCNT0";
    private static final String PING_PONG_TR_ID = "PINGPONG";

    @Mock
    private KisWebSocketMessageParser kisWebSocketMessageParser;

    @Mock
    private KisWebSocketHandler kisWebSocketHandler;

    @Mock
    private WebSocketSession session;

    private KisWebSocketDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        when(kisWebSocketHandler.getTrId()).thenReturn(TR_ID);
        dispatcher = new KisWebSocketDispatcher(kisWebSocketMessageParser, List.of(kisWebSocketHandler));
    }

    @Nested
    @DisplayName("연결 수립/종료")
    class ConnectionLifecycle {
        @Test
        @DisplayName("연결이 수립되면 예외 없이 처리된다")
        void successAfterConnectionEstablished() {
            // given
            when(session.getId()).thenReturn("sessionId1");

            // when & then
            assertThatCode(() -> dispatcher.afterConnectionEstablished(session))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("연결이 종료되면 예외 없이 처리된다")
        void successAfterConnectionClosed() {
            // given
            when(session.getId()).thenReturn("sessionId1");

            // when & then
            assertThatCode(() -> dispatcher.afterConnectionClosed(session, org.springframework.web.socket.CloseStatus.NORMAL))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("전송 계층 오류가 발생하면 예외 없이 처리된다")
        void successHandleTransportError() {
            // given
            when(session.getId()).thenReturn("sessionId1");

            // when & then
            assertThatCode(() -> dispatcher.handleTransportError(session, new IllegalStateException("transport error")))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("메타 메시지 처리")
    class MetaMessage {
        @Test
        @DisplayName("PingPong 메시지를 수신하면 동일한 페이로드를 그대로 응답한다")
        void successEchoPingPongMessage() throws Exception {
            // given
            String payload = "{\"header\":{\"tr_id\":\"PINGPONG\"}}";
            TextMessage message = new TextMessage(payload);
            KisWebSocketMetaResponseDto metaResponse = new KisWebSocketMetaResponseDto(
                    new KisWebSocketMetaResponseDto.Header(PING_PONG_TR_ID), null
            );
            when(kisWebSocketMessageParser.parseMeta(payload)).thenReturn(metaResponse);

            // when
            dispatcher.handleTextMessage(session, message);

            // then
            ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
            verify(session).sendMessage(captor.capture());
            assertThat(captor.getValue().getPayload()).isEqualTo(payload);

            verify(kisWebSocketMessageParser, never()).parse(anyString());
            verify(kisWebSocketHandler, never()).handle(any());
        }

        @Test
        @DisplayName("PingPong이 아닌 메타 메시지(구독 응답 등)를 수신하면 응답하지 않고 실시간 데이터로도 처리하지 않는다")
        void successHandleNonPingPongMetaMessage() throws Exception {
            // given
            String payload = "{\"header\":{\"tr_id\":\"H0STCNT0\"},\"body\":{\"rt_cd\":\"0\",\"msg1\":\"SUBSCRIBE SUCCESS\"}}";
            TextMessage message = new TextMessage(payload);
            KisWebSocketMetaResponseDto metaResponse = new KisWebSocketMetaResponseDto(
                    new KisWebSocketMetaResponseDto.Header(TR_ID),
                    new KisWebSocketMetaResponseDto.Body("0", "OPSP0000", "SUBSCRIBE SUCCESS", null)
            );
            when(kisWebSocketMessageParser.parseMeta(payload)).thenReturn(metaResponse);

            // when
            dispatcher.handleTextMessage(session, message);

            // then
            verify(session, never()).sendMessage(any());
            verify(kisWebSocketMessageParser, never()).parse(anyString());
            verify(kisWebSocketHandler, never()).handle(any());
        }

        @Test
        @DisplayName("메타 메시지의 body가 없어도 예외 없이 처리된다")
        void successHandleMetaMessageWithoutBody() throws Exception {
            // given
            String payload = "{\"header\":{\"tr_id\":\"H0STCNT0\"}}";
            TextMessage message = new TextMessage(payload);
            KisWebSocketMetaResponseDto metaResponse = new KisWebSocketMetaResponseDto(
                    new KisWebSocketMetaResponseDto.Header(TR_ID), null
            );
            when(kisWebSocketMessageParser.parseMeta(payload)).thenReturn(metaResponse);

            // when & then
            assertThatCode(() -> dispatcher.handleTextMessage(session, message))
                    .doesNotThrowAnyException();

            verify(session, never()).sendMessage(any());
        }
    }

    @Nested
    @DisplayName("실시간 데이터 메시지 처리")
    class DataMessage {
        @Test
        @DisplayName("TR_ID에 해당하는 핸들러가 등록되어 있으면 해당 핸들러에게 처리를 위임한다")
        void successDelegateToRegisteredHandler() throws Exception {
            // given
            String payload = "1|H0STCNT0|001|005930^091530^70000";
            TextMessage message = new TextMessage(payload);
            KisWebSocketResponseDto response = KisWebSocketResponseDto.builder()
                    .isEncoded(true)
                    .trId(TR_ID)
                    .dataCount(1)
                    .data("005930^091530^70000")
                    .build();
            when(kisWebSocketMessageParser.parse(payload)).thenReturn(response);

            // when
            dispatcher.handleTextMessage(session, message);

            // then
            verify(kisWebSocketHandler).handle(response);
        }

        @Test
        @DisplayName("TR_ID에 해당하는 핸들러가 없으면 처리를 위임하지 않는다")
        void doNothingWhenHandlerNotRegistered() throws Exception {
            // given
            String payload = "1|UNKNOWN|001|005930^091530^70000";
            TextMessage message = new TextMessage(payload);
            KisWebSocketResponseDto response = KisWebSocketResponseDto.builder()
                    .isEncoded(true)
                    .trId("UNKNOWN")
                    .dataCount(1)
                    .data("005930^091530^70000")
                    .build();
            when(kisWebSocketMessageParser.parse(payload)).thenReturn(response);

            // when
            dispatcher.handleTextMessage(session, message);

            // then
            verify(kisWebSocketHandler, never()).handle(any());
        }
    }
}
