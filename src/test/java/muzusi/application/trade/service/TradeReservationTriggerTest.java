package muzusi.application.trade.service;

import muzusi.application.stock.dto.StockPriceDto;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import muzusi.infrastructure.redis.constant.TradeConstant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradeReservationTriggerTest {

    @InjectMocks
    private TradeReservationTrigger tradeReservationTrigger;

    @Mock
    private TradeReservationProcessor tradeReservationProcessor;
    @Mock
    private RedisService redisService;

    @Test
    @DisplayName("예약된 종목 코드가 없는 경우 트리거 실행 안 됨")
    void triggerTradeReservationsNoStockCode() {
        // given
        given(redisService.getSetMembers(TradeConstant.RESERVATION_PREFIX.getValue())).willReturn(Set.of());

        // when
        tradeReservationTrigger.triggerTradeReservations();

        // then
        verify(tradeReservationProcessor, never()).processTradeReservations(any(), any(), any());
    }

    @Test
    @DisplayName("예약된 종목 코드가 있는 경우 정상 처리")
    void triggerTradeReservationsWithStockCode() {
        // given
        String stockCode = "005930";
        StockPriceDto stockPriceDto = StockPriceDto.builder()
                .low(2900L)
                .high(3100L)
                .build();

        given(redisService.getSetMembers(TradeConstant.RESERVATION_PREFIX.getValue())).willReturn(Set.of(stockCode));
        given(redisService.getHash(KisConstant.INQUIRE_PRICE_PREFIX.getValue(), stockCode)).willReturn(stockPriceDto);

        // when
        tradeReservationTrigger.triggerTradeReservations();

        // then
        verify(tradeReservationProcessor, times(1)).processTradeReservations(stockCode, 2900L, 3100L);
    }

    @Test
    @DisplayName("예약된 종목 코드에 대한 현재가가 없는 경우")
    void triggerTradeReservationsNoStockPrice() {
        // given
        String stockCode = "005930";
        StockPriceDto stockPriceDto = null;

        given(redisService.getSetMembers(TradeConstant.RESERVATION_PREFIX.getValue())).willReturn(Set.of(stockCode));
        given(redisService.getHash(KisConstant.INQUIRE_PRICE_PREFIX.getValue(), stockCode)).willReturn(stockPriceDto);

        // when
        tradeReservationTrigger.triggerTradeReservations();

        // then
        verify(tradeReservationProcessor, never()).processTradeReservations(any(), any(), any());
    }
}
