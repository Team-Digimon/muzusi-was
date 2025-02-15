package muzusi.application.stock.dto;

import lombok.Builder;
import muzusi.domain.stock.entity.StockDaily;
import muzusi.domain.stock.entity.StockMonthly;
import muzusi.domain.stock.entity.StockWeekly;
import muzusi.domain.stock.entity.StockYearly;

import java.time.LocalDateTime;

@Builder
public record StockChartInfoDto(
        String stockCode,
        LocalDateTime date,
        Long open,
        Long high,
        Long low,
        Long close,
        Long volume
) {
    public static StockChartInfoDto from(StockDaily stockDaily) {
        return new StockChartInfoDto(
                stockDaily.getStockCode(), stockDaily.getDate(),
                stockDaily.getOpen(), stockDaily.getHigh(),
                stockDaily.getLow(), stockDaily.getClose(),
                stockDaily.getVolume()
        );
    }

    public static StockChartInfoDto from(StockWeekly stockWeekly) {
        return new StockChartInfoDto(
                stockWeekly.getStockCode(), stockWeekly.getDate(),
                stockWeekly.getOpen(), stockWeekly.getHigh(),
                stockWeekly.getLow(), stockWeekly.getClose(),
                stockWeekly.getVolume()
        );
    }

    public static StockChartInfoDto from(StockMonthly stockMonthly) {
        return new StockChartInfoDto(
                stockMonthly.getStockCode(), stockMonthly.getDate(),
                stockMonthly.getOpen(), stockMonthly.getHigh(),
                stockMonthly.getLow(), stockMonthly.getClose(),
                stockMonthly.getVolume()
        );
    }

    public static StockChartInfoDto from(StockYearly stockYearly) {
        return new StockChartInfoDto(
                stockYearly.getStockCode(), stockYearly.getDate(),
                stockYearly.getOpen(), stockYearly.getHigh(),
                stockYearly.getLow(), stockYearly.getClose(),
                stockYearly.getVolume()
        );
    }
}
