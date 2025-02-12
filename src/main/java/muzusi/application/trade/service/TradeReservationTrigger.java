package muzusi.application.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.StockPriceDto;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import muzusi.infrastructure.redis.constant.TradeConstant;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeReservationTrigger {
    private final TradeReservationProcessor tradeReservationProcessor;
    private final RedisService redisService;

    /**
     * 예약 내역 확인 로직 실행 메서드
     * 1. 예약 내역이 있는 종목 코드 가져오기.
     * 2. 해당 종목의 특정 시간대 저가, 고가 가져오기.
     * 3. 처리.
     */
    public void triggerTradeReservations() {
        redisService.getSetMembers(TradeConstant.RESERVATION_PREFIX.getValue())
                .stream()
                .map(Object::toString)
                .forEach(stockCode -> {
                    StockPriceDto stockPriceDto =
                            (StockPriceDto) redisService.getHash(KisConstant.INQUIRE_PRICE_PREFIX.getValue(), stockCode);
                    tradeReservationProcessor.processTradeReservations(stockCode, stockPriceDto.low(), stockPriceDto.high());
                });
    }
}
