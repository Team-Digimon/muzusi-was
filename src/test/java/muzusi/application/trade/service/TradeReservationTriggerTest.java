package muzusi.application.trade.service;

import muzusi.domain.account.entity.Account;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.service.HoldingService;
import muzusi.domain.trade.entity.Trade;
import muzusi.domain.trade.entity.TradeReservation;
import muzusi.domain.trade.service.TradeReservationService;
import muzusi.domain.trade.service.TradeService;
import muzusi.domain.trade.type.TradeType;
import muzusi.domain.user.entity.User;
import muzusi.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private TradeReservationService tradeReservationService;
    @Mock
    private AccountService accountService;
    @Mock
    private HoldingService holdingService;
    @Mock
    private UserService userService;
    @Mock
    private TradeService tradeService;

    private TradeReservation buyReservation;
    private TradeReservation sellReservation;
    private Account account;
    private Holding holding;
    private User user;

    @BeforeEach
    void setUp() {
        buyReservation = TradeReservation.builder()
                .tradeType(TradeType.BUY)
                .inputPrice(3000L)
                .stockCount(5)
                .stockName("삼성전자")
                .stockCode("005390")
                .userId(1L)
                .build();

        sellReservation = TradeReservation.builder()
                .tradeType(TradeType.SELL)
                .inputPrice(3000L)
                .stockCount(3)
                .stockName("삼성전자")
                .stockCode("005390")
                .userId(1L)
                .build();

        account = Account.builder()
                .balance(Account.INITIAL_BALANCE)
                .build();
        account.increaseReservedPrice(3000L * 5);

        holding = Holding.builder()
                .stockCode("005390")
                .stockName("삼성전자")
                .stockCount(10)
                .averagePrice(2900L)
                .account(account)
                .build();
        holding.increaseReservedStock(3);

        user = User.builder()
                .username("testUser")
                .build();
    }

    @Test
    @DisplayName("예약된 매수 주문이 처리됨")
    void processBuyOrderSuccess() {
        // given
        given(tradeReservationService.readByStockCode("005390")).willReturn(List.of(buyReservation));
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));
        given(holdingService.readByUserIdAndStockCode(1L, "005390")).willReturn(Optional.of(holding));
        int expectedCount = holding.getStockCount() + buyReservation.getStockCount();
        long expectedPrice = ((holding.getAveragePrice() * holding.getStockCount()) +
                (buyReservation.getInputPrice() * buyReservation.getStockCount())) / expectedCount;

        // when
        tradeReservationTrigger.processTradeReservations("005390", 2900L);

        // then
        assertEquals(0, account.getReservedPrice());
        assertEquals(expectedCount, holding.getStockCount());
        assertEquals(expectedPrice, holding.getAveragePrice());

        verify(holdingService, never()).save(any(Holding.class));
        verify(tradeReservationService, times(1)).deleteById(buyReservation.getId());
        verify(tradeService, times(1)).save(any(Trade.class));
    }

    @Test
    @DisplayName("예약된 매도 주문이 처리됨")
    void processSellOrderSuccess() {
        // given
        given(tradeReservationService.readByStockCode("005390")).willReturn(List.of(sellReservation));
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));
        given(holdingService.readByUserIdAndStockCode(1L, "005390")).willReturn(Optional.of(holding));
        long expectedBalance = account.getBalance() + (sellReservation.getStockCount() * sellReservation.getInputPrice());
        int expectedCount = holding.getStockCount() - sellReservation.getStockCount();

        // when
        tradeReservationTrigger.processTradeReservations("005390", 3100L);

        // then
        assertEquals(expectedBalance, account.getBalance());
        assertEquals(expectedCount, holding.getStockCount());

        verify(tradeReservationService, times(1)).deleteById(sellReservation.getId());
        verify(tradeService, times(1)).save(any(Trade.class));
    }

    @Test
    @DisplayName("보유 주식이 없는 경우 새로운 Holding 생성 후 매수 처리")
    void processBuyOrderCreateNewHolding() {
        // given
        Holding newHolding = Holding.builder()
                .stockCount(0)
                .averagePrice(0L)
                .build();
        given(tradeReservationService.readByStockCode("005390")).willReturn(List.of(buyReservation));
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));
        given(holdingService.readByUserIdAndStockCode(1L, "005390")).willReturn(Optional.empty());
        given(userService.readById(1L)).willReturn(Optional.of(user));
        given(holdingService.save(any(Holding.class))).willReturn(newHolding);

        // when
        tradeReservationTrigger.processTradeReservations("005390", 2900L);

        // then
        assertEquals(buyReservation.getStockCount(), newHolding.getStockCount());
        assertEquals(buyReservation.getInputPrice(), newHolding.getAveragePrice());

        verify(holdingService, times(1)).save(any(Holding.class));
        verify(tradeReservationService, times(1)).deleteById(buyReservation.getId());
        verify(tradeService, times(1)).save(any(Trade.class));
    }

    @Test
    @DisplayName("매도 후 보유 주식이 0이 되면 삭제")
    void processSellOrderDeleteHoldingWhenZero() {
        // given
        TradeReservation fullSellReservation = TradeReservation.builder()
                .tradeType(TradeType.SELL)
                .inputPrice(3000L)
                .stockCount(10)
                .stockName("삼성전자")
                .stockCode("005390")
                .userId(1L)
                .build();

        given(tradeReservationService.readByStockCode("005390")).willReturn(List.of(fullSellReservation));
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));
        given(holdingService.readByUserIdAndStockCode(1L, "005390")).willReturn(Optional.of(holding));
        long expectedBalance = account.getBalance() + (fullSellReservation.getInputPrice() * fullSellReservation.getStockCount());

        // when
        tradeReservationTrigger.processTradeReservations("005390", 3100L);

        // then
        assertEquals(expectedBalance, account.getBalance());

        verify(holdingService, times(1)).deleteByUserIdAndStockCode(1L, "005390");
        verify(tradeReservationService, times(1)).deleteById(fullSellReservation.getId());
        verify(tradeService, times(1)).save(any(Trade.class));
    }
}