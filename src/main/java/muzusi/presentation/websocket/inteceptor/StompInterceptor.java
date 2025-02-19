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