package muzusi.application.trade.service;

import muzusi.application.trade.dto.TradeReqDto;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.exception.HoldingErrorType;
import muzusi.domain.holding.service.HoldingService;
import muzusi.domain.trade.entity.TradeReservation;
import muzusi.domain.trade.service.TradeReservationService;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradeReservationHandlerTest {

    @InjectMocks
    private TradeReservationHandler tradeReservationHandler;

    @Mock
    private TradeReservationService tradeReservationService;
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
        buyTradeDto = new TradeReqDto(3500L, 3000L, 5, "000610", TradeType.BUY);
        sellTradeDto = new TradeReqDto(2500L, 3000L, 3, "000610", TradeType.SELL);

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
    @DisplayName("매수 예약 저장 테스트")
    void handleReservationPurchaseTest() {
        // given
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));

        // when
        tradeReservationHandler.saveTradeReservation(1L, buyTradeDto);

        // then
        assertEquals(Account.INITIAL_BALANCE - (buyTradeDto.stockPrice() * buyTradeDto.stockCount()), account.getBalance());
        verify(tradeReservationService, times(1)).save(any(TradeReservation.class));
    }

    @Test
    @DisplayName("매도 예약 저장 테스트")
    void handleReservationSaleTest() {
        // given
        given(holdingService.readByUserIdAndStockCode(1L, buyTradeDto.stockCode())).willReturn(Optional.of(holding));

        // when
        tradeReservationHandler.saveTradeReservation(1L, sellTradeDto);

        // then
        verify(tradeReservationService, times(1)).save(any(TradeReservation.class));
    }

    @Test
    @DisplayName("매도 예약 실패 테스트 - 수량 부족")
    void handleReservationSaleFalseTest() {
        // given
        TradeReqDto overCountDto = new TradeReqDto(2500L, 3000L, 15, "000610", TradeType.SELL);
        given(holdingService.readByUserIdAndStockCode(1L, buyTradeDto.stockCode())).willReturn(Optional.of(holding));

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                tradeReservationHandler.saveTradeReservation(1L, overCountDto)
        );

        // then
        assertEquals(HoldingErrorType.INSUFFICIENT_STOCK.getStatus(), exception.getErrorType().getStatus());
        assertEquals(HoldingErrorType.INSUFFICIENT_STOCK.getCode(), exception.getErrorType().getCode());
        assertEquals(HoldingErrorType.INSUFFICIENT_STOCK.getMessage(), exception.getErrorType().getMessage());
    }
}
