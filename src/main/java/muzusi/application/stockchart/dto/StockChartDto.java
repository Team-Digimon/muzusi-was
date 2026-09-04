package muzusi.application.stockchart.dto;

import lombok.Builder;
import muzusi.application.stockprice.dto.StockPriceDto;
import muzusi.domain.stockcandle.entity.StockDailyCandle;
import muzusi.domain.stockcandle.entity.StockMinuteCandle;
import muzusi.global.util.datetime.DateTimeFormatterUtil;

@Builder
public record StockChartDto(
        String stockCode,
        String dateTime,
        Long open,
        Long high,
        Long low,
        Long close,
        Long volume
) {
    public static StockChartDto from(StockMinuteCandle stockMinuteCandle) {
        return StockChartDto.builder()
                .stockCode(stockMinuteCandle.getStockCode())
                .dateTime(DateTimeFormatterUtil.parseToString(stockMinuteCandle.getDateTime()))
                .open(stockMinuteCandle.getOpen())
                .high(stockMinuteCandle.getHigh())
                .low(stockMinuteCandle.getLow())
                .close(stockMinuteCandle.getClose())
                .volume(stockMinuteCandle.getVolume())
                .build();
    }
    
    public static StockChartDto from(StockDailyCandle stockDailyCandle) {
        return StockChartDto.builder()
                .stockCode(stockDailyCandle.getId().getStockCode())
                .dateTime(DateTimeFormatterUtil.parseToString(stockDailyCandle.getId().getDate().atStartOfDay()))
                .open(stockDailyCandle.getOpen())
                .high(stockDailyCandle.getHigh())
                .low(stockDailyCandle.getLow())
                .close(stockDailyCandle.getClose())
                .volume(stockDailyCandle.getVolume())
                .build();
    }
    
    public StockPriceDto toStockPriceDto() {
        return StockPriceDto.builder()
                .stockCode(stockCode)
                .high(high)
                .low(low)
                .close(close)
                .build();
    }
}
