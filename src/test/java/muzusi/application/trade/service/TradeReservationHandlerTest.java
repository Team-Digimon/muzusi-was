package muzusi.application.trade.service;

import muzusi.application.trade.dto.TradeReqDto;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.exception.AccountErrorType;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.exception.HoldingErrorType;
import muzusi.domain.holding.service.HoldingService;
import muzusi.domain.trade.entity.TradeReservation;
import muzusi.domain.trade.exception.TradeErrorType;
import muzusi.domain.trade.service.TradeReservationService;
import muzusi.domain.trade.type.TradeType;
import muzusi.global.exception.CustomException;
import muzusi.infrastructure.redis.RedisService;
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
    @Mock
    private RedisService redisService;

    private TradeReqDto buyTradeDto;
    private TradeReqDto sellTradeDto;
    private Account account;
    private Holding holding;
    private TradeReservation buyTradeReservation;
    private TradeReservation sellTradeReservation;

    @BeforeEach
    void setUp() {
        buyTradeDto = new TradeReqDto(3500L, 3000L, 5, "삼성전자", "005390", TradeType.BUY);
        sellTradeDto = new TradeReqDto(2500L, 3000L, 3, "삼성전자", "005390", TradeType.SELL);

        account = Account.builder()
                .balance(Account.INITIAL_BALANCE)
                .build();

        holding = Holding.builder()
                .stockCode("000610")
                .stockCount(10)
                .averagePrice(2900L)
                .account(account)
                .build();

        buyTradeReservation = TradeReservation.builder()
                .userId(1L)
                .inputPrice(3000L)
                .stockCount(10)
                .stockCode("000610")
                .tradeType(TradeType.BUY)
                .build();

        sellTradeReservation = TradeReservation.builder()
                .userId(1L)
                .inputPrice(3000L)
                .stockCount(10)
                .stockCode("000610")
                .tradeType(TradeType.SELL)
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
        assertEquals(Account.INITIAL_BALANCE - (buyTradeDto.inputPrice() * buyTradeDto.stockCount()), account.getBalance());
        assertEquals(buyTradeDto.inputPrice() * buyTradeDto.stockCount(), account.getReservedPrice());
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
        assertEquals(sellTradeDto.stockCount(), holding.getReservedStockCount());
        verify(tradeReservationService, times(1)).save(any(TradeReservation.class));
    }

    @Test
    @DisplayName("매수 예약 실패 테스트 - 잔액 부족")
    void handleReservationPurchaseFailInsufficientBalance() {
        // given
        Account lowBalanceAccount = Account.builder()
                .balance(5000L)
                .user(null)
                .build();

        given(accountService.readByUserId(1L)).willReturn(Optional.of(lowBalanceAccount));

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                tradeReservationHandler.saveTradeReservation(1L, buyTradeDto)
        );

        // then
        assertEquals(AccountErrorType.INSUFFICIENT_BALANCE.getStatus(), exception.getErrorType().getStatus());
        assertEquals(AccountErrorType.INSUFFICIENT_BALANCE.getCode(), exception.getErrorType().getCode());
        assertEquals(AccountErrorType.INSUFFICIENT_BALANCE.getMessage(), exception.getErrorType().getMessage());
    }

    @Test
    @DisplayName("매도 예약 실패 테스트 - 수량 부족")
    void handleReservationSaleFalseTest() {
        // given
        TradeReqDto overCountDto = new TradeReqDto(2500L, 3000L, 15, "삼성전자", "005390", TradeType.SELL);
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

    @Test
    @DisplayName("예약 매수 취소 테스트")
    void cancelReservationPurchaseTest() {
        // given
        Long tradeReservationId = 1L;
        given(tradeReservationService.readById(tradeReservationId)).willReturn(Optional.ofNullable(buyTradeReservation));
        given(accountService.readByUserId(1L)).willReturn(Optional.ofNullable(account));
        Long currentAccountBalance = account.getBalance();
        Long currentAccountReservedPrice = account.getReservedPrice();
        long reservationPrice = buyTradeReservation.getInputPrice() * buyTradeReservation.getStockCount();

        // when
        tradeReservationHandler.cancelTradeReservation(1L, tradeReservationId);

        // then
        assertEquals(currentAccountBalance + (reservationPrice), account.getBalance());
        assertEquals(currentAccountReservedPrice - (reservationPrice), account.getReservedPrice());
        verify(tradeReservationService, times(1)).deleteById(tradeReservationId);
    }

    @Test
    @DisplayName("예약 매도 취소 테스트")
    void cancelReservationSaleTest() {
        // given
        Long tradeReservationId = 1L;
        given(tradeReservationService.readById(tradeReservationId)).willReturn(Optional.ofNullable(sellTradeReservation));
        given(holdingService.readByUserIdAndStockCode(1L, "000610")).willReturn(Optional.ofNullable(holding));
        int currentReservedStockCount = holding.getReservedStockCount();

        // when
        tradeReservationHandler.cancelTradeReservation(1L, tradeReservationId);

        // then
        assertEquals(currentReservedStockCount - sellTradeReservation.getStockCount(), holding.getReservedStockCount());
        verify(tradeReservationService, times(1)).deleteById(tradeReservationId);
    }

    @Test
    @DisplayName("예약 취소에 대한 권한 없음")
    void cancelReservationFailedByForbidden() {
        // given
        Long tradeReservationId = 1L;
        given(tradeReservationService.readById(tradeReservationId)).willReturn(Optional.ofNullable(sellTradeReservation));
        Long anotherUserId = 2L;

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                tradeReservationHandler.cancelTradeReservation(anotherUserId, tradeReservationId)
        );

        // then
        assertEquals(TradeErrorType.UNAUTHORIZED_ACCESS.getStatus(), exception.getErrorType().getStatus());
        assertEquals(TradeErrorType.UNAUTHORIZED_ACCESS.getCode(), exception.getErrorType().getCode());
        assertEquals(TradeErrorType.UNAUTHORIZED_ACCESS.getMessage(), exception.getErrorType().getMessage());
    }
}
