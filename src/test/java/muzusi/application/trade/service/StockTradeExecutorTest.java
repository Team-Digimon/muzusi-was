package muzusi.application.trade.service;

import muzusi.domain.stock.service.StockService;
import muzusi.application.trade.dto.TradeReqDto;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.exception.AccountErrorType;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.exception.HoldingErrorType;
import muzusi.domain.holding.service.HoldingService;
import muzusi.domain.stock.entity.Stock;
import muzusi.domain.trade.entity.Trade;
import muzusi.domain.trade.service.TradeService;
import muzusi.domain.trade.type.TradeType;
import muzusi.domain.user.entity.User;
import muzusi.domain.user.service.UserService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockTradeExecutorTest {

    @InjectMocks
    private StockTradeExecutor stockTradeExecutor;

    @Mock
    private TradeService tradeService;
    @Mock
    private StockService stockService;
    @Mock
    private AccountService accountService;
    @Mock
    private HoldingService holdingService;
    @Mock
    private UserService userService;

    private TradeReqDto buyTradeDto;
    private TradeReqDto sellTradeDto;
    private Account account;
    private Stock stock;
    private Holding holding;
    private User user;

    @BeforeEach
    void setUp() {
        buyTradeDto = new TradeReqDto(3000L, 3000L, 5, "000610", TradeType.BUY);
        sellTradeDto = new TradeReqDto(3000L, 3000L, 3, "000610", TradeType.SELL);

        account = Account.builder()
                .balance(Account.INITIAL_BALANCE)
                .build();

        stock = Stock.builder()
                .stockName("삼성전자")
                .stockCode("000610")
                .build();

        holding = Holding.builder()
                .stockCode("000610")
                .stockCount(10)
                .averagePrice(2900L)
                .account(account)
                .build();

        user = User.builder()
                .username("test")
                .nickname("테스터")
                .build();
    }

    @Test
    @DisplayName("매수 실행 테스트 - 새로운 내역 추가")
    void executeTradeBuyNewHolding() {
        // given
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));
        given(stockService.readByStockCode(buyTradeDto.stockCode())).willReturn(Optional.of(stock));
        given(userService.readById(1L)).willReturn(Optional.of(user));
        given(holdingService.existsByUserIdAndStockCode(1L, buyTradeDto.stockCode())).willReturn(false);

        // when
        stockTradeExecutor.executeTrade(1L, buyTradeDto);

        // then
        assertEquals(Account.INITIAL_BALANCE - (buyTradeDto.stockPrice() * buyTradeDto.stockCount()), account.getBalance());
        verify(holdingService, times(1)).save(any(Holding.class));
        verify(tradeService, times(1)).save(any(Trade.class));
    }

    @Test
    @DisplayName("매수 실행 테스트 - 기존 보유 주식 업데이트")
    void executeTradeBuyExistingHolding() {
        // given
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));
        given(stockService.readByStockCode(buyTradeDto.stockCode())).willReturn(Optional.of(stock));
        given(holdingService.existsByUserIdAndStockCode(1L, buyTradeDto.stockCode())).willReturn(true);
        given(holdingService.readByUserIdAndStockCode(1L, buyTradeDto.stockCode())).willReturn(Optional.of(holding));

        int prevStockCount = holding.getStockCount();
        long prevTotalCost = prevStockCount * holding.getAveragePrice();

        // when
        stockTradeExecutor.executeTrade(1L, buyTradeDto);

        // then
        int expectedStockCount = prevStockCount + buyTradeDto.stockCount();
        long expectedTotalCost = prevTotalCost + (buyTradeDto.stockCount() * buyTradeDto.stockPrice());
        long expectedAveragePrice = expectedTotalCost / expectedStockCount;

        assertEquals(Account.INITIAL_BALANCE - (buyTradeDto.stockPrice() * buyTradeDto.stockCount()), account.getBalance());
        assertEquals(expectedStockCount, holding.getStockCount());
        assertEquals(expectedAveragePrice, holding.getAveragePrice());

        verify(holdingService, never()).save(any(Holding.class));
        verify(tradeService, times(1)).save(any(Trade.class));
    }

    @Test
    @DisplayName("매도 실행 테스트 - 충분한 수량 보유")
    void executeTradeSellSuccess() {
        // given
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));
        given(stockService.readByStockCode(sellTradeDto.stockCode())).willReturn(Optional.of(stock));
        given(holdingService.readByUserIdAndStockCode(1L, sellTradeDto.stockCode())).willReturn(Optional.of(holding));

        // when
        stockTradeExecutor.executeTrade(1L, sellTradeDto);

        // then
        assertEquals(Account.INITIAL_BALANCE + (sellTradeDto.stockPrice() * sellTradeDto.stockCount()), account.getBalance());
        verify(holdingService, never()).deleteByUserIdAndStockCode(1L, sellTradeDto.stockCode());
        verify(tradeService, times(1)).save(any(Trade.class));
    }

    @Test
    @DisplayName("매도 실행 실패 - 보유 주식 없음")
    void executeTradeSellFailNoHolding() {
        // given
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                stockTradeExecutor.executeTrade(1L, sellTradeDto)
        );

        // then
        assertEquals(HoldingErrorType.NOT_FOUND.getStatus(), exception.getErrorType().getStatus());
        assertEquals(HoldingErrorType.NOT_FOUND.getCode(), exception.getErrorType().getCode());
        assertEquals(HoldingErrorType.NOT_FOUND.getMessage(), exception.getErrorType().getMessage());
    }

    @Test
    @DisplayName("매도 실행 테스트 - 보유 주식이 0이 되면 삭제")
    void executeTradeSellDeleteHoldingWhenZero() {
        // given
        TradeReqDto fullSellTradeDto = new TradeReqDto(3000L, 3000L, holding.getStockCount(), "000610", TradeType.SELL);

        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));
        given(stockService.readByStockCode(fullSellTradeDto.stockCode())).willReturn(Optional.of(stock));
        given(holdingService.readByUserIdAndStockCode(1L, fullSellTradeDto.stockCode())).willReturn(Optional.of(holding));

        // when
        stockTradeExecutor.executeTrade(1L, fullSellTradeDto);

        // then
        verify(holdingService, times(1)).deleteByUserIdAndStockCode(1L, fullSellTradeDto.stockCode());
        verify(tradeService, times(1)).save(any(Trade.class));
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
                stockTradeExecutor.executeTrade(1L, buyTradeDto)
        );

        // then
        assertEquals(AccountErrorType.INSUFFICIENT_BALANCE.getStatus(), exception.getErrorType().getStatus());
        assertEquals(AccountErrorType.INSUFFICIENT_BALANCE.getCode(), exception.getErrorType().getCode());
        assertEquals(AccountErrorType.INSUFFICIENT_BALANCE.getMessage(), exception.getErrorType().getMessage());
    }

    @Test
    @DisplayName("매도 실행 실패 - 보유 주식 부족")
    void executeTradeSellFailInsufficientStock() {
        // given
        given(accountService.readByUserId(1L)).willReturn(Optional.of(account));
        given(holdingService.readByUserIdAndStockCode(1L, sellTradeDto.stockCode())).willReturn(Optional.of(holding));
        TradeReqDto invalidSellTradeDto = new TradeReqDto(3000L, 3000L, 100, "000610", TradeType.SELL);

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                stockTradeExecutor.executeTrade(1L, invalidSellTradeDto)
        );

        // then
        assertEquals(HoldingErrorType.INSUFFICIENT_STOCK.getStatus(), exception.getErrorType().getStatus());
        assertEquals(HoldingErrorType.INSUFFICIENT_STOCK.getCode(), exception.getErrorType().getCode());
        assertEquals(HoldingErrorType.INSUFFICIENT_STOCK.getMessage(), exception.getErrorType().getMessage());
    }
}