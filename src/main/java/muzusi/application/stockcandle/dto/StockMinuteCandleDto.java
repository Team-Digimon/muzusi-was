package muzusi.application.stockcandle.dto;

import lombok.Builder;
import muzusi.domain.stockcandle.entity.StockMinuteCandle;

import java.time.LocalDateTime;

@Builder
public record StockMinuteCandleDto(
        String stockCode,
        LocalDateTime dateTime,
        Long open,
        Long high,
        Long low,
        Long close,
        Long volume
) {
    public StockMinuteCandle toEntity() {
        return StockMinuteCandle.builder()
                .stockCode(stockCode)
                .dateTime(dateTime)
                .open(open)
                .high(high)
                .low(low)
                .close(close)
                .volume(volume)
                .build();
    }
}
