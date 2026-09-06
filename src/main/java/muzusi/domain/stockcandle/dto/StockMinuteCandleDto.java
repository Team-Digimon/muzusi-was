package muzusi.domain.stockcandle.dto;

import java.time.LocalDateTime;

public record StockMinuteCandleDto(
        String stockCode,
        LocalDateTime dateTime,
        Long open,
        Long high,
        Long low,
        Long close,
        Long volume
) { }
