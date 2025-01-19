package muzusi.application.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.service.StockService;
import muzusi.application.trade.dto.TradeReqDto;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.exception.AccountErrorType;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.exception.HoldingErrorType;
import muzusi.domain.holding.service.HoldingService;
import muzusi.domain.stock.entity.Stock;
import muzusi.domain.stock.exception.StockErrorType;
import muzusi.domain.trade.entity.Trade;
import muzusi.domain.trade.service.TradeService;
import muzusi.domain.trade.type.TradeType;
import muzusi.global.exception.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StockTradeExecutor {
    private final TradeService tradeService;
    private final StockService stockService;
    private final AccountService accountService;
    private final HoldingService holdingService;

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

        if (tradeReqDto.tradeType() == TradeType.BUY)
            handleStockPurchase(tradeReqDto, account);

        if (tradeReqDto.tradeType() == TradeType.SELL)
            handleStockSale(tradeReqDto, account);

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

    /**
     * 매수에 대한 내역 처리 메서드
     * 금액이 부족하다 -> 예외처리
     * 해당 주식에 대한 내역이 있다 -> 그 데이터를 기반으로 업데이트
     * 해당 주식에 대한 내역이 없다 -> 새로운 관리 데이터 생성
     *
     * @param tradeReqDto : trade 정보 dto
     * @param account : 연결된 계좌
     */
    private void handleStockPurchase(TradeReqDto tradeReqDto, Account account) {
        long price = tradeReqDto.stockPrice() * tradeReqDto.stockCount();

        if (account.getBalance() < price)
            throw new CustomException(AccountErrorType.INSUFFICIENT_BALANCE);

        if (!holdingService.existsByStockCode(tradeReqDto.stockCode())) {
            holdingService.save(
                    Holding.builder()
                            .stockCode(tradeReqDto.stockCode())
                            .stockCount(tradeReqDto.stockCount())
                            .averagePrice(tradeReqDto.stockPrice())
                            .account(account)
                            .build()
            );
        } else {
            Holding holding = holdingService.readByStockCode(tradeReqDto.stockCode()).get();
            holding.addStock(tradeReqDto.stockCount(), tradeReqDto.stockPrice());
        }

        account.updateAccount(tradeReqDto.tradeType(), price);
    }

    /**
     * 매도에 대한 내역 처리 메서드
     * 매도로 인해 주식의 수량이 0이 되면 삭제.
     *
     * @param tradeReqDto : trade 정보 dto
     * @param account : 연결된 계좌
     */
    private void handleStockSale(TradeReqDto tradeReqDto, Account account) {
        Holding holding = holdingService.readByStockCode(tradeReqDto.stockCode())
                .orElseThrow(() -> new CustomException(HoldingErrorType.NOT_FOUND));

        if (!holding.sellStock(tradeReqDto.stockCount()))
            throw new CustomException(HoldingErrorType.INSUFFICIENT_STOCK);

        if (holding.isEmpty())
            holdingService.deleteByStockCode(tradeReqDto.stockCode());

        long price = tradeReqDto.stockPrice() * tradeReqDto.stockCount();
        account.updateAccount(tradeReqDto.tradeType(), price);
    }
}
