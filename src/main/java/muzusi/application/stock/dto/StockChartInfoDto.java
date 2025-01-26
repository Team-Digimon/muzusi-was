package muzusi.application.stock.dto;

import muzusi.domain.stock.entity.StockDaily;
import muzusi.domain.stock.entity.StockMonthly;
import muzusi.domain.stock.entity.StockWeekly;
import muzusi.domain.stock.entity.StockYearly;

public record StockChartInfoDto(
        String stockCode,
        String date,
        Double open,
        Double high,
        Double low,
        Double close,
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
