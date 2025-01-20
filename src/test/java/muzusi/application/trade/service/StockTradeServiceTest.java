package muzusi.application.trade.service;

import muzusi.application.trade.dto.TradeReqDto;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.exception.AccountErrorType;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.exception.HoldingErrorType;
import muzusi.domain.holding.service.HoldingService;
import muzusi.domain.trade.type.TradeType;
import muzusi.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
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
    @Mock
    private AccountService accountService;
    @Mock
    private HoldingService holdingService;

    private TradeReqDto buyTradeDto;
    private TradeReqDto sellTradeDto;
    private Account account;
    private Holding holding;

    @BeforeEach
    void setUp() {
        buyTradeDto = new TradeReqDto(3000L, 3100L, 5, "000610", TradeType.BUY);
        sellTradeDto = new TradeReqDto(3000L, 2900L, 3, "000610", TradeType.SELL);

        account = Account.builder()
                .balance(Account.INITIAL_BALANCE)
                .build();

        holding = Holding.builder()
                .stockCode("000610")
                .stockCount(10)
                .averagePrice(2900L)
                .account(account)
                .build();
    }

    @Test
    @DisplayName("즉시 매수 테스트")
    void tradeStockBuyExecute() {
        // given
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));

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
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));

        // when
        stockTradeService.tradeStock(1L, buyReservationDto);

        // then
        verify(stockTradeExecutor, never()).executeTrade(anyLong(), any(TradeReqDto.class));
        verify(tradeReservationHandler, times(1)).saveTradeReservation(1L, buyReservationDto);
    }

    @Test
    @DisplayName("매수 실행 실패 - 잔액 부족")
    void executeTradeBuyFailInsufficientBalance() {
        // given
        Account lowBalanceAccount = Account.builder()
                .balance(5000L)
                .user(null)
                .build();

        given(accountService.readByUserId(1L)).willReturn(Optional.of(lowBalanceAccount));

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                stockTradeService.tradeStock(1L, buyTradeDto)
        );

        // then
        assertEquals(AccountErrorType.INSUFFICIENT_BALANCE.getStatus(), exception.getErrorType().getStatus());
        assertEquals(AccountErrorType.INSUFFICIENT_BALANCE.getCode(), exception.getErrorType().getCode());
        assertEquals(AccountErrorType.INSUFFICIENT_BALANCE.getMessage(), exception.getErrorType().getMessage());
    }

    @Test
    @DisplayName("즉시 매도 테스트")
    void tradeStockSellExecute() {
        // given
        given(holdingService.readByUserIdAndStockCode(1L, buyTradeDto.stockCode())).willReturn(Optional.of(holding));

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
        given(holdingService.readByUserIdAndStockCode(1L, buyTradeDto.stockCode())).willReturn(Optional.of(holding));

        // when
        stockTradeService.tradeStock(1L, sellReservationDto);

        // then
        verify(stockTradeExecutor, never()).executeTrade(anyLong(), any(TradeReqDto.class));
        verify(tradeReservationHandler, times(1)).saveTradeReservation(1L, sellReservationDto);
    }

    @Test
    @DisplayName("매도 실행 실패 - 보유 주식 부족")
    void executeTradeSellFailInsufficientStock() {
        // given
        given(holdingService.readByUserIdAndStockCode(1L, sellTradeDto.stockCode())).willReturn(Optional.of(holding));
        TradeReqDto invalidSellTradeDto = new TradeReqDto(3000L, 3000L, 100, "000610", TradeType.SELL);

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                stockTradeService.tradeStock(1L, invalidSellTradeDto)
        );

        // then
        assertEquals(HoldingErrorType.INSUFFICIENT_STOCK.getStatus(), exception.getErrorType().getStatus());
        assertEquals(HoldingErrorType.INSUFFICIENT_STOCK.getCode(), exception.getErrorType().getCode());
        assertEquals(HoldingErrorType.INSUFFICIENT_STOCK.getMessage(), exception.getErrorType().getMessage());
    }
}
