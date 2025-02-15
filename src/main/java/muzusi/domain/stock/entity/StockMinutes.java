package muzusi.domain.stock.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muzusi.application.stock.dto.StockChartInfoDto;
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

    private List<StockChartInfoDto> minutesChart;

    @Builder
    public StockMinutes(String stockCode, LocalDate date, List<StockChartInfoDto> minutesChart) {
        this.stockCode = stockCode;
        this.date = date;
        this.minutesChart = minutesChart;
    }
}