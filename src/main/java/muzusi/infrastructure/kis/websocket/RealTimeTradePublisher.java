package muzusi.infrastructure.kis.websocket;

import lombok.RequiredArgsConstructor;
import muzusi.application.trade.dto.TradeNotificationDto;
import muzusi.domain.trade.type.TradeType;
import muzusi.infrastructure.redis.constant.ChannelConstant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RealTimeTradePublisher{
    private final RedisTemplate redisTemplate;

    public void execute(String payload) {
        String[] parts = payload.split("\\^");
        String[] metas = parts[0].split("\\|");
        int tradeCount = Integer.parseInt(metas[2]);
        String stockCode = metas[3];

        for (int idx = 0, i = 0; i < tradeCount; i++) {
            publish(TradeNotificationDto.builder()
                    .stockCode(stockCode)
                    .time(convertTime(parts[idx + 1]))
                    .price(Long.valueOf(parts[idx + 2]))
                    .stockCount(Long.valueOf(parts[idx + 12]))
                    .volume(Long.valueOf(parts[idx + 13]))
                    .tradeType((parts[idx + 21].equals("1")) ? TradeType.BUY : TradeType.SELL)
                    .changeRate(Double.valueOf(parts[idx + 5]))
                    .build());
            idx += 46;
        }
    }

    private String convertTime(String time) {
        return time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);
    }

    private void publish(TradeNotificationDto tradeNotification) {
        redisTemplate.convertAndSend(ChannelConstant.TRADE.getValue(), tradeNotification);
    }
}