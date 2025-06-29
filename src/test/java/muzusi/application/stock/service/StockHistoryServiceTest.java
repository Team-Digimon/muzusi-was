package muzusi.application.stock.service;

import muzusi.application.stock.dto.StockChartInfoDto;
import muzusi.domain.stock.exception.StockErrorType;
import muzusi.domain.stock.service.StockDailyService;
import muzusi.domain.stock.service.StockMinutesService;
import muzusi.domain.stock.service.StockMonthlyService;
import muzusi.domain.stock.service.StockWeeklyService;
import muzusi.domain.stock.service.StockYearlyService;
import muzusi.domain.stock.type.StockPeriodType;
import muzusi.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
@ExtendWith(MockitoExtension.class)
public class StockHistoryServiceTest {
    
    @Mock
    private StockMinutesService stockMinutesService;
    
    @Mock
    private StockDailyService stockDailyService;
    
    @Mock
    private StockWeeklyService stockWeeklyService;
    
    @Mock
    private StockMonthlyService stockMonthlyService;
    
    @Mock
    private StockYearlyService stockYearlyService;
    
    @InjectMocks
    private StockHistoryService stockHistoryService;
    
    private final String stockCode = "000001";
    
    @Test
    @DisplayName("당일 분봉 조회 - 성공")
    void successToGetTodayMinutesChart() {
        // given
        StockChartInfoDto stockChartInfoDto = StockChartInfoDto.builder()
                .stockCode(stockCode)
                .date(LocalDateTime.of(2025, 6, 30, 9, 0))
                .open(10000L)
                .high(11000L)
                .low(9000L)
                .close(9000L)
                .volume(30000L)
                .build();
        
        given(stockMinutesService.readAllInCache(stockCode))
                .willReturn(List.of(stockChartInfoDto));
        
        try (MockedStatic<LocalDateTime> mockedLocalDateTime
                     = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)
        ) {
            String date = "2025-06-30T12:00:00Z";
            Clock clock = Clock.fixed(Instant.parse(date), ZoneId.of("UTC"));
            LocalDateTime mockNow = LocalDateTime.now(clock);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(mockNow);
            
            // when
            List<StockChartInfoDto> result = stockHistoryService
                    .getStockHistoryByType(stockCode, StockPeriodType.MINUTES_TODAY);
            
            // then
            assertEquals(result.size(), 1);
        }
    }
    
    @Test
    @DisplayName("당일 분봉 조회 실패: 조회 불가능 시간 - 시간 외 조회")
    void failToGetTodayMinutesChartTime() {
        // given
        try (MockedStatic<LocalDateTime> mockedLocalDateTime
                     = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)
        ) {
            String date = "2025-06-30T06:00:00Z";
            Clock clock = Clock.fixed(Instant.parse(date), ZoneId.of("UTC"));
            LocalDateTime mockNow = LocalDateTime.now(clock);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(mockNow);
            
            // when
            CustomException exception = assertThrows(CustomException.class, ()
                    -> stockHistoryService.getStockHistoryByType(stockCode, StockPeriodType.MINUTES_TODAY));
            
            // then
            assertEquals(exception.getErrorType(), StockErrorType.NOT_AVAILABLE_MINUTES_CHART);
        }
    }
    
    @Test
    @DisplayName("당일 분봉 조회 실패: 조회 불가능 시간 - 주말")
    void failToGetTodayMinutesChartWeekend() {
        // given
        try (MockedStatic<LocalDateTime> mockedLocalDateTime
                     = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)
        ) {
            String date = "2025-06-29T12:00:00Z";
            Clock clock = Clock.fixed(Instant.parse(date), ZoneId.of("UTC"));
            LocalDateTime mockNow = LocalDateTime.now(clock);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(mockNow);
            
            // when
            CustomException exception = assertThrows(CustomException.class, ()
                    -> stockHistoryService.getStockHistoryByType(stockCode, StockPeriodType.MINUTES_TODAY));
            
            // then
            assertEquals(exception.getErrorType(), StockErrorType.NOT_AVAILABLE_MINUTES_CHART);
        }
    }
}
