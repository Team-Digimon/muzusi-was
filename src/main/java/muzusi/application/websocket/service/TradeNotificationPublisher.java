package muzusi.application.websocket.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.trade.dto.TradeNotificationDto;
import muzusi.infrastructure.redis.constant.ChannelConstant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TradeNotificationPublisher {
    private final RedisTemplate redisTemplate;
    
    /**
     * 한국투자증권 국내 주식 체결가 정보 목록을 Redis Pub/Sub 토픽으로 발행(Publish)하는 메서드
     *
     * @param tradeNotifications    주식 체결가 정보 목록
     */
    public void publishTradeNotification(List<TradeNotificationDto> tradeNotifications) {
        for (TradeNotificationDto tradeNotification : tradeNotifications) {
            this.publishTradeNotification(tradeNotification);
        }
    }
    
    /**
     * 한국투자증권 국내 주식 체결가 정보를 Redis Pub/Sub 토픽으로 발행(Publish)하는 메서드
     *
     * @param tradeNotification     주식 체결가 정보
     */
    private void publishTradeNotification(TradeNotificationDto tradeNotification) {
        redisTemplate.convertAndSend(ChannelConstant.TRADE.getValue(), tradeNotification);
    }
}