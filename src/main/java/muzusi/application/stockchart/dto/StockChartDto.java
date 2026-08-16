package muzusi.application.stockchart.dto;

import lombok.Builder;
import muzusi.application.stock.dto.StockPriceDto;
import muzusi.domain.stock.entity.StockDaily;
import muzusi.domain.stock.entity.StockMonthly;
import muzusi.domain.stock.entity.StockWeekly;
import muzusi.domain.stock.entity.StockYearly;
import muzusi.global.util.datetime.DateTimeFormatterUtil;

@Builder
public record StockChartDto (
        String stockCode,
        String dateTime,
        Long open,
        Long high,
        Long low,
        Long close,
        Long volume
) {
    public static StockChartDto from(StockDaily stockDaily) {
        return new StockChartDto(
                stockDaily.getStockCode(), DateTimeFormatterUtil.parseToString(stockDaily.getDate()),
                stockDaily.getOpen(), stockDaily.getHigh(),
                stockDaily.getLow(), stockDaily.getClose(),
                stockDaily.getVolume()
        );
    }

    public static StockChartDto from(StockWeekly stockWeekly) {
        return new StockChartDto(
                stockWeekly.getStockCode(), DateTimeFormatterUtil.parseToString(stockWeekly.getDate()),
                stockWeekly.getOpen(), stockWeekly.getHigh(),
                stockWeekly.getLow(), stockWeekly.getClose(),
                stockWeekly.getVolume()
        );
    }

    public static StockChartDto from(StockMonthly stockMonthly) {
        return new StockChartDto(
                stockMonthly.getStockCode(), DateTimeFormatterUtil.parseToString(stockMonthly.getDate()),
                stockMonthly.getOpen(), stockMonthly.getHigh(),
                stockMonthly.getLow(), stockMonthly.getClose(),
                stockMonthly.getVolume()
        );
    }

    public static StockChartDto from(StockYearly stockYearly) {
        return new StockChartDto(
                stockYearly.getStockCode(), DateTimeFormatterUtil.parseToString(stockYearly.getDate()),
                stockYearly.getOpen(), stockYearly.getHigh(),
                stockYearly.getLow(), stockYearly.getClose(),
                stockYearly.getVolume()
        );
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
