package muzusi.application.stockcandle.service;

import muzusi.domain.stockcandle.service.StockMinuteCandleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockMinuteCandleCleanerTest {

    private static final int RETENTION_DAYS = 7;

    @Mock
    private StockMinuteCandleService stockMinuteCandleService;

    @InjectMocks
    private StockMinuteCandleCleaner stockMinuteCandleCleaner;

    @Test
    @DisplayName("오래된 분봉 삭제 - 보관 기간(7일) 전 자정을 기준 시각으로 삭제를 위임한다")
    void deleteOutdatedStockMinuteCandle() {
        // given
        given(stockMinuteCandleService.deleteByDateTimeBefore(any(LocalDateTime.class))).willReturn(120);
        LocalDate expectedThresholdDate = LocalDate.now().minusDays(RETENTION_DAYS);

        // when
        stockMinuteCandleCleaner.deleteOutdatedStockMinuteCandle();

        // then
        ArgumentCaptor<LocalDateTime> thresholdCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(stockMinuteCandleService).deleteByDateTimeBefore(thresholdCaptor.capture());

        LocalDateTime threshold = thresholdCaptor.getValue();
        assertThat(threshold.toLocalDate()).isEqualTo(expectedThresholdDate);
        assertThat(threshold.toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
    }
}
