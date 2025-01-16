package muzusi.application.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.service.StockService;
import muzusi.application.trade.dto.TradeReqDto;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.exception.AccountErrorType;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.stock.entity.Stock;
import muzusi.domain.stock.exception.StockErrorType;
import muzusi.domain.trade.entity.Trade;
import muzusi.domain.trade.service.TradeService;
import muzusi.global.exception.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StockTradeExecutor {
    private final TradeService tradeService;
    private final StockService stockService;
    private final AccountService accountService;

    /**
     * 주식 매수, 매도 메서드
     *
     * @param userId : 사용자 pk값
     * @param tradeReqDto : trade 정보 dto
     */
    @Transactional
    public void executeTrade(Long userId, TradeReqDto tradeReqDto) {
        Account account = accountService.readByUserId(userId)
                .orElseThrow(() -> new CustomException(AccountErrorType.NOT_FOUND));

        Stock stock = stockService.readByStockCode(tradeReqDto.stockCode())
                .orElseThrow(() -> new CustomException(StockErrorType.NOT_FOUND));

        tradeService.save(
                Trade.builder()
                        .stockPrice(tradeReqDto.stockPrice())
                        .stockCount(tradeReqDto.stockCount())
                        .tradeType(tradeReqDto.tradeType())
                        .stock(stock)
                        .account(account)
                        .build()
        );
    }
}
