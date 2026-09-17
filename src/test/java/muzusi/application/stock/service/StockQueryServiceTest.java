package muzusi.application.stock.service;

import muzusi.application.stock.dto.StockResponse;
import muzusi.domain.stock.entity.Stock;
import muzusi.domain.stock.exception.StockErrorType;
import muzusi.domain.stock.service.StockService;
import muzusi.domain.stock.type.MarketType;
import muzusi.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StockQueryServiceTest {
    @Mock
    private StockService stockService;

    @InjectMocks
    private StockQueryService stockQueryService;

    private static final String STOCK_CODE = "005930";

    @Test
    @DisplayName("존재하는 종목코드로 조회하면 주식 기본 정보를 반환한다")
    void getStock() {
        // given
        Stock stock = Stock.builder()
                .stockCode(STOCK_CODE)
                .stockName("삼성전자")
                .marketType(MarketType.KOSPI)
                .build();

        given(stockService.readByStockCode(STOCK_CODE)).willReturn(Optional.of(stock));

        // when
        StockResponse result = stockQueryService.getStock(STOCK_CODE);

        // then
        assertThat(result.stockCode()).isEqualTo(STOCK_CODE);
        assertThat(result.stockName()).isEqualTo("삼성전자");
        assertThat(result.marketType()).isEqualTo(MarketType.KOSPI);
    }

    @Test
    @DisplayName("존재하지 않는 종목코드로 조회하면 예외가 발생한다")
    void getStock_notFound() {
        // given
        given(stockService.readByStockCode(STOCK_CODE)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> stockQueryService.getStock(STOCK_CODE))
                .isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getErrorType())
                .isEqualTo(StockErrorType.NOT_FOUND);
    }
}
