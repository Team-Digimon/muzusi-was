package muzusi.presentation.websocket.inteceptor;

import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.kis.KisRealTimeTradeHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {
    private final KisRealTimeTradeHandler kisRealTimeTradeHandler;

    /**
     * 특정 종목 구독 및 해제 시 한국투자증권 웹소켓 연결 관리를 위한 메서드
     *
     * - 구독 등록 시, 한국투자증권 주식 체결가 웹 소켓 등록 요청
     * - 구독 해제 시, 한국투자증권 주식 체결가 웹 소켓 해제 요청
     *
     * @param message : 수신 메시지
     * @param channel : 메시지 채널
     * @return        : 기본 처리 메서드 호출
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String stockCode = accessor.getDestination().replace("/sub/","");
            kisRealTimeTradeHandler.connect(stockCode);
        }

        if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
            String stockCode = accessor.getDestination().replace("/unsub/","");
            kisRealTimeTradeHandler.disconnect(stockCode);
        }

        return ChannelInterceptor.super.preSend(message, channel);
    }
}