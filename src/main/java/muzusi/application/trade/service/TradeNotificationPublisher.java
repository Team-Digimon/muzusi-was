package muzusi.application.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.trade.dto.TradeNotificationDto;
import muzusi.infrastructure.redis.constant.ChannelConstant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeNotificationPublisher {
    private final RedisTemplate redisTemplate;

    public void publishTradeNotification(TradeNotificationDto tradeNotification) {
        redisTemplate.convertAndSend(ChannelConstant.TRADE.getValue(), tradeNotification);
    }
}