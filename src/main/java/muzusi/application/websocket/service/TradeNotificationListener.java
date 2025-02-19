package muzusi.application.websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.trade.dto.TradeNotificationDto;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeNotificationListener implements MessageListener {
    private final RedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String received =(String) redisTemplate.getStringSerializer().deserialize(message.getBody());
            TradeNotificationDto tradeNotification = objectMapper.readValue(received, TradeNotificationDto.class);

            messagingTemplate.convertAndSend("/sub/" + tradeNotification.stockCode(), tradeNotification);
        } catch(Exception e) {
            log.error(e.getMessage());
        }
    }
}