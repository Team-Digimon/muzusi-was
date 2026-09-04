package muzusi.presentation.stockchart.api;

import api.link.checker.annotation.ApiGroup;
import api.link.checker.annotation.TrackApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import muzusi.domain.stock.type.StockPeriodType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@ApiGroup(value = "[주식 차트 조회 API]")
@Tag(name = "[주식 차트 조회 API]", description = "주식 차트 조회 관련 API")
public interface StockChartApi {

    @TrackApi(description = "주식 차트 조회")
    @Operation(
            summary = "주식 차트 조회",
            description = """
                    주식 차트를 조회하는 API입니다.

                    - period: 조회 단위 (MINUTES, DAILY, WEEKLY, MONTHLY, YEARLY)
                    - from, to: 조회 기간 (yyyy-MM-dd, 둘 다 선택). 생략 시 서버 기본값 적용
                        - to 생략 시: 오늘
                        - from 생략 시: DAILY 1년 전 / WEEKLY 3년 전 / MONTHLY 5년 전 / YEARLY 10년 전
                    - MINUTES는 from, to를 무시하고 최근 수집분(당일 및 지난 7일)을 전부 반환합니다.
                    - WEEKLY, MONTHLY, YEARLY는 일봉을 서버에서 집계하여 반환하며,
                      요청 기간은 해당 주/월/년의 경계까지 확장되어 경계에 걸친 봉도 온전한 값으로 집계됩니다.
                    - 응답의 dateTime은 "yyyy-MM-dd HH:mm:ss" 형식이며, 일/주/월/년봉은 시각이 00:00:00 입니다.
                      (주/월/년봉의 dateTime은 각각 해당 주의 월요일 / 해당 월 1일 / 해당 년 1월 1일)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주식 차트 조회 성공 (분봉/일봉/주봉/월봉/년봉 응답 필드 동일)",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                                "code": 200,
                                                "message": "요청이 성공하였습니다.",
                                                "data": [
                                                    {
                                                         "stockCode": "005930",
                                                         "dateTime": "2024-08-27 00:00:00",
                                                         "open": 75700,
                                                         "high": 76500,
                                                         "low": 75600,
                                                         "close": 75800,
                                                         "volume": 11130145
                                                    },
                                                    {
                                                         "stockCode": "005930",
                                                         "dateTime": "2024-08-28 00:00:00",
                                                         "open": 75800,
                                                         "high": 76400,
                                                         "low": 75400,
                                                         "close": 76400,
                                                         "volume": 9794514
                                                    },
                                                    {
                                                         "stockCode": "005930",
                                                         "dateTime": "2024-08-29 00:00:00",
                                                         "open": 73600,
                                                         "high": 74700,
                                                         "low": 73500,
                                                         "close": 74000,
                                                         "volume": 16884479
                                                    }
                                                ]
                                            }
                                    """)
                    }))
    })
    ResponseEntity<?> getStockHistory(
            @Parameter(description = "주식 코드", required = true, example = "005930")
            @PathVariable String stockCode,

            @Parameter(description = "조회 단위", required = true, example = "DAILY")
            @RequestParam StockPeriodType period,

            @Parameter(description = "조회 시작일 (yyyy-MM-dd, 선택). 생략 시 period별 기본값", example = "2024-01-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "조회 종료일 (yyyy-MM-dd, 선택). 생략 시 오늘", example = "2024-12-31")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    );
}
