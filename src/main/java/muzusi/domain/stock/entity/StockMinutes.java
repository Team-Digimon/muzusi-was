package muzusi.domain.stock.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muzusi.application.stock.dto.StockMinutesChartInfoDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "stock_minutes")
public class StockMinutes {
    @Id
    private String id;

    private String stockCode;

    private LocalDate date;

    private List<StockMinutesChartInfoDto> minutesChart;

    @Builder
    public StockMinutes(String stockCode, LocalDate date, List<StockMinutesChartInfoDto> minutesChart) {
        this.stockCode = stockCode;
        this.date = date;
        this.minutesChart = minutesChart;
    }
}