package muzusi.application.trade.service;

import muzusi.application.trade.dto.TradeReqDto;
import muzusi.domain.trade.type.TradeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockTradeServiceTest {

    @InjectMocks
    private StockTradeService stockTradeService;

    @Mock
    private StockTradeExecutor stockTradeExecutor;
    @Mock
    private TradeReservationHandler tradeReservationHandler;

    private TradeReqDto buyTradeDto;
    private TradeReqDto sellTradeDto;

    @BeforeEach
    void setUp() {
        buyTradeDto = new TradeReqDto(3000L, 3100L, 5, "000610", TradeType.BUY);
        sellTradeDto = new TradeReqDto(3000L, 2900L, 3, "000610", TradeType.SELL);
    }

    @Test
    @DisplayName("즉시 매수 테스트")
    void tradeStockBuyExecute() {
        // given

        // when
        stockTradeService.tradeStock(1L, buyTradeDto);

        // then
        verify(stockTradeExecutor, times(1)).executeTrade(1L, buyTradeDto);
        verify(tradeReservationHandler, never()).saveTradeReservation(anyLong(), any(TradeReqDto.class));
    }

    @Test
    @DisplayName("매수 예약 테스트")
    void tradeStockBuyReservation() {
        // given
        TradeReqDto buyReservationDto = new TradeReqDto(3000L, 2900L, 5, "000610", TradeType.BUY);

        // when
        stockTradeService.tradeStock(1L, buyReservationDto);

        // then
        verify(stockTradeExecutor, never()).executeTrade(anyLong(), any(TradeReqDto.class));
        verify(tradeReservationHandler, times(1)).saveTradeReservation(1L, buyReservationDto);
    }

    @Test
    @DisplayName("즉시 매도 테스트")
    void tradeStockSellExecute() {
        // when
        stockTradeService.tradeStock(1L, sellTradeDto);

        // then
        verify(stockTradeExecutor, times(1)).executeTrade(1L, sellTradeDto);
        verify(tradeReservationHandler, never()).saveTradeReservation(anyLong(), any(TradeReqDto.class));
    }

    @Test
    @DisplayName("매도 예약 테스트")
    void tradeStockSellReservation() {
        // given
        TradeReqDto sellReservationDto = new TradeReqDto(3000L, 3100L, 3, "000610", TradeType.SELL);
        // when
        stockTradeService.tradeStock(1L, sellReservationDto);

        // then
        verify(stockTradeExecutor, never()).executeTrade(anyLong(), any(TradeReqDto.class));
        verify(tradeReservationHandler, times(1)).saveTradeReservation(1L, sellReservationDto);
    }
}
