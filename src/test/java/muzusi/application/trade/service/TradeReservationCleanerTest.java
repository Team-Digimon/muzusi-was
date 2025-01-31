package muzusi.application.trade.service;

import muzusi.domain.account.entity.Account;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.service.HoldingService;
import muzusi.domain.trade.entity.TradeReservation;
import muzusi.domain.trade.service.TradeReservationService;
import muzusi.domain.trade.type.TradeType;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradeReservationCleanerTest {

    @InjectMocks
    private TradeReservationCleaner tradeReservationCleaner;

    @Mock
    private TradeReservationService tradeReservationService;
    @Mock
    private AccountService accountService;
    @Mock
    private HoldingService holdingService;

    private TradeReservation buyReservation1;
    private TradeReservation buyReservation2;
    private TradeReservation sellReservation1;
    private TradeReservation sellReservation2;
    private Account account1;
    private Account account2;
    private Holding holding1;
    private Holding holding2;

    @BeforeEach
    void setUp() {
        buyReservation1 = TradeReservation.builder()
                .userId(1L)
                .inputPrice(3000L)
                .stockCount(5)
                .stockCode("005390")
                .tradeType(TradeType.BUY)
                .build();

        buyReservation2 = TradeReservation.builder()
                .userId(2L)
                .inputPrice(3000L)
                .stockCount(5)
                .stockCode("005390")
                .tradeType(TradeType.BUY)
                .build();

        sellReservation1 = TradeReservation.builder()
                .userId(1L)
                .inputPrice(2500L)
                .stockCount(3)
                .stockCode("005390")
                .tradeType(TradeType.SELL)
                .build();

        sellReservation2 = TradeReservation.builder()
                .userId(2L)
                .inputPrice(2500L)
                .stockCount(3)
                .stockCode("005390")
                .tradeType(TradeType.SELL)
                .build();

        account1 = Account.builder()
                .balance(Account.INITIAL_BALANCE)
                .build();
        account1.increaseReservedPrice(3000L * 5);

        holding1 = Holding.builder()
                .stockCode("005390")
                .stockCount(10)
                .account(account1)
                .build();
        holding1.increaseReservedStock(3);

        account2 = Account.builder()
                .balance(Account.INITIAL_BALANCE)
                .build();
        account2.increaseReservedPrice(3000L * 5);

        holding2 = Holding.builder()
                .stockCode("005390")
                .stockCount(10)
                .account(account1)
                .build();
        holding2.increaseReservedStock(3);
    }

    @Test
    @DisplayName("미처리 예약 삭제 및 계좌/보유 주식 업데이트 테스트")
    void clearReservedOrdersAtMarketCloseTest() {
        // given
        given(tradeReservationService.readAll()).willReturn(
                List.of(buyReservation1, buyReservation2, sellReservation1, sellReservation2)
        );
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account1));
        given(accountService.readByUserId(2L)).willReturn(Optional.of(account2));
        given(holdingService.readByUserIdAndStockCode(1L, "005390")).willReturn(Optional.of(holding1));
        given(holdingService.readByUserIdAndStockCode(2L, "005390")).willReturn(Optional.of(holding2));


        // when
        tradeReservationCleaner.clearReservedOrdersAtMarketClose();

        // then
        assertEquals(0, account1.getReservedPrice());
        assertEquals(0, account2.getReservedPrice());
        assertEquals(0, holding1.getReservedStockCount());
        assertEquals(0, holding2.getReservedStockCount());

        verify(tradeReservationService, times(1)).deleteAll();
    }
}