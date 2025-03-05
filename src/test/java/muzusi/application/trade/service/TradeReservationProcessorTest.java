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
import muzusi.infrastructure.redis.RedisService;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradeReservationProcessorTest {

    @InjectMocks
    private TradeReservationProcessor tradeReservationProcessor;

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
    @Mock
    private RedisService redisService;

    private TradeReservation buyReservation1;
    private TradeReservation buyReservation2;
    private TradeReservation sellReservation1;
    private TradeReservation sellReservation2;
    private Account account;
    private Holding holding;
    private User user;

    @BeforeEach
    void setUp() {
        buyReservation1 = TradeReservation.builder()
                .tradeType(TradeType.BUY)
                .inputPrice(3000L)
                .stockCount(5)
                .stockName("삼성전자")
                .stockCode("005390")
                .userId(1L)
                .build();

        buyReservation2 = TradeReservation.builder()
                .tradeType(TradeType.BUY)
                .inputPrice(3000L)
                .stockCount(5)
                .stockName("삼성전자")
                .stockCode("005390")
                .userId(1L)
                .build();

        sellReservation1 = TradeReservation.builder()
                .tradeType(TradeType.SELL)
                .inputPrice(3000L)
                .stockCount(3)
                .stockName("삼성전자")
                .stockCode("005390")
                .userId(1L)
                .build();

        sellReservation2 = TradeReservation.builder()
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
        account.increaseReservedPrice(3000L * 5 * 2);

        holding = Holding.builder()
                .stockCode("005390")
                .stockName("삼성전자")
                .stockCount(10)
                .averagePrice(2900L)
                .account(account)
                .build();
        holding.increaseReservedStock(6);

        user = User.builder()
                .username("testUser")
                .build();
    }

    @Test
    @DisplayName("예약된 매수 주문이 처리됨")
    void processBuyOrderSuccess() {
        // given
        given(tradeReservationService.readByStockCode("005390")).willReturn(List.of(buyReservation1, buyReservation2));
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));
        given(holdingService.readByUserIdAndStockCode(1L, "005390")).willReturn(Optional.of(holding));
        long totalPrice = (buyReservation1.getInputPrice() * buyReservation1.getStockCount())
                + (buyReservation2.getInputPrice() * buyReservation2.getStockCount());
        int totalCount = buyReservation1.getStockCount() + buyReservation2.getStockCount();
        int expectedCount = holding.getStockCount() + totalCount;
        long expectedPrice = ((holding.getAveragePrice() * holding.getStockCount()) +
                totalPrice) / expectedCount;

        // when
        tradeReservationProcessor.processTradeReservations("005390", 3000L, 3100L);

        // then
        assertEquals(0, account.getReservedPrice());
        assertEquals(expectedCount, holding.getStockCount());
        assertEquals(expectedPrice, holding.getAveragePrice());

        verify(accountService, times(1)).readByUserId(1L);
        verify(holdingService, times(1)).readByUserIdAndStockCode(1L, "005390");
        verify(holdingService, never()).save(any(Holding.class));
        verify(tradeReservationService, times(1)).deleteAllByIds(anyList());
        verify(tradeService, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("예약된 매도 주문이 처리됨")
    void processSellOrderSuccess() {
        // given
        given(tradeReservationService.readByStockCode("005390")).willReturn(List.of(sellReservation1, sellReservation2));
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));
        given(holdingService.readByUserIdAndStockCode(1L, "005390")).willReturn(Optional.of(holding));
        long totalPrice = (sellReservation1.getInputPrice() * sellReservation1.getStockCount())
                + (sellReservation2.getInputPrice() * sellReservation2.getStockCount());
        int totalCount = sellReservation1.getStockCount() + sellReservation2.getStockCount();
        long expectedBalance = account.getBalance() + totalPrice;
        int expectedCount = holding.getStockCount() - totalCount;

        // when
        tradeReservationProcessor.processTradeReservations("005390", 3000L, 3100L);

        // then
        assertEquals(expectedBalance, account.getBalance());
        assertEquals(expectedCount, holding.getStockCount());

        verify(accountService, times(1)).readByUserId(1L);
        verify(holdingService, times(1)).readByUserIdAndStockCode(1L, "005390");
        verify(tradeReservationService, times(1)).deleteAllByIds(anyList());
        verify(tradeService, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("보유 주식이 없는 경우 새로운 Holding 생성 후 매수 처리")
    void processBuyOrderCreateNewHolding() {
        // given
        Holding newHolding = Holding.builder()
                .stockCount(0)
                .averagePrice(0L)
                .build();
        given(tradeReservationService.readByStockCode("005390")).willReturn(List.of(buyReservation1, buyReservation2));
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));
        given(holdingService.readByUserIdAndStockCode(1L, "005390")).willReturn(Optional.empty());
        given(userService.readById(1L)).willReturn(Optional.of(user));
        given(holdingService.save(any(Holding.class))).willReturn(newHolding);
        long totalPrice = (buyReservation1.getInputPrice() * buyReservation1.getStockCount())
                + (buyReservation2.getInputPrice() * buyReservation2.getStockCount());
        int totalCount = buyReservation1.getStockCount() + buyReservation2.getStockCount();
        long averagePrice = totalPrice / totalCount;

        // when
        tradeReservationProcessor.processTradeReservations("005390", 2900L, 3100L);

        // then
        assertEquals(totalCount, newHolding.getStockCount());
        assertEquals(averagePrice, newHolding.getAveragePrice());

        verify(accountService, times(1)).readByUserId(1L);
        verify(holdingService, times(1)).readByUserIdAndStockCode(1L, "005390");
        verify(holdingService, times(1)).save(any(Holding.class));
        verify(tradeReservationService, times(1)).deleteAllByIds(anyList());
        verify(tradeService, times(1)).saveAll(anyList());
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
        tradeReservationProcessor.processTradeReservations("005390", 3000L, 3100L);

        // then
        assertEquals(expectedBalance, account.getBalance());

        verify(accountService, times(1)).readByUserId(1L);
        verify(holdingService, times(1)).readByUserIdAndStockCode(1L, "005390");
        verify(holdingService, times(1)).deleteByUserIdAndStockCode(1L, "005390");
        verify(tradeReservationService, times(1)).deleteAllByIds(anyList());
        verify(tradeService, times(1)).saveAll(anyList());
    }
}