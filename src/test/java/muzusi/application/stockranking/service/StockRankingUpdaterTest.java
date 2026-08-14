package muzusi.application.stockranking.service;

import com.google.common.util.concurrent.RateLimiter;
import muzusi.application.stock.dto.StockRankDto;
import muzusi.application.stockranking.port.FetchStockRankingPort;
import muzusi.domain.stock.service.StockRankingService;
import muzusi.global.util.datetime.DateTimeFormatterUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockRankingUpdaterTest {

    @Mock
    private FetchStockRankingPort fetchStockRankingPort;

    @Mock
    private StockRankingService stockRankingService;

    @Mock
    private RateLimiter kisRateLimiter;

    @InjectMocks
    private StockRankingUpdater stockRankingUpdater;

    private StockRankDto stockRankDto(String code, int rank) {
        return StockRankDto.builder()
                .name("삼성전자")
                .code(code)
                .rank(rank)
                .price(70000L)
                .prdyVrss(1000L)
                .prdyCtrt(1.5)
                .avrgVol(1000000L)
                .build();
    }

    @Test
    @DisplayName("거래량 기준 주식 순위 갱신 - 성공")
    void successToUpdateVolumeRank() {
        // given
        List<StockRankDto> volumeRank = List.of(stockRankDto("005930", 1));
        given(fetchStockRankingPort.getVolumeRank()).willReturn(volumeRank);

        try (MockedStatic<LocalDateTime> mockedLocalDateTime
                     = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            LocalDateTime fixedNow = LocalDateTime.of(2025, 6, 30, 12, 0, 0);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedNow);

            // when
            stockRankingUpdater.updateVolumeRank();

            // then
            verify(kisRateLimiter, times(1)).acquire();
            verify(fetchStockRankingPort).getVolumeRank();
            verify(stockRankingService).deleteVolumeRankingCache();
            verify(stockRankingService).setVolumeRankingInCache(volumeRank);
            verify(stockRankingService)
                    .setVolumeRankingTimeInCache(DateTimeFormatterUtil.parseToString(fixedNow));
        }
    }

    @Test
    @DisplayName("급등락 기준 주식 순위 갱신 - 성공")
    void successToUpdateFluctuationRank() {
        // given
        List<StockRankDto> risingStockRanking = List.of(stockRankDto("005930", 1));
        List<StockRankDto> fallingStockRanking = List.of(stockRankDto("000660", 1));
        given(fetchStockRankingPort.getRisingFluctuationRank()).willReturn(risingStockRanking);
        given(fetchStockRankingPort.getFallingFluctuationRank()).willReturn(fallingStockRanking);

        try (MockedStatic<LocalDateTime> mockedLocalDateTime
                     = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            LocalDateTime fixedNow = LocalDateTime.of(2025, 6, 30, 12, 0, 0);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedNow);

            // when
            stockRankingUpdater.updateFluctuationRank();

            // then
            verify(kisRateLimiter, times(1)).acquire();
            verify(fetchStockRankingPort).getRisingFluctuationRank();
            verify(fetchStockRankingPort).getFallingFluctuationRank();
            verify(stockRankingService).deleteRisingRankingInCache();
            verify(stockRankingService).deleteFallingRankingInCache();
            verify(stockRankingService).setRisingRankingInCache(risingStockRanking);
            verify(stockRankingService).setFallingRankingInCache(fallingStockRanking);
            verify(stockRankingService)
                    .setFallingAndRisingRankingTimeInCache(DateTimeFormatterUtil.parseToString(fixedNow));
        }
    }
}
