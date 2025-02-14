package muzusi.presentation.stock.api;

import api.link.checker.annotation.ApiGroup;
import api.link.checker.annotation.TrackApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import muzusi.application.stock.dto.StockMinutesPeriodDto;
import muzusi.domain.stock.type.StockPeriodType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@ApiGroup(value = "[주식 차트 조회 API]")
@Tag(name = "[주식 차트 조회 API]", description = "주식 차트 조회 관련 API")
public interface StockHistoryApi {

    @TrackApi(description = "주식 차트 조회")
    @Operation(summary = "주식 차트 조회", description = "주식 차트를 조회하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주식 차트 조회 성공(일/주/월/년 응답값 필드 동일)",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                    {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": [
                                                {
                                                     "stockCode": "005930",
                                                     "date": "2024-08-27",
                                                     "open": 75700.0,
                                                     "high": 76500.0,
                                                     "low": 75600.0,
                                                     "close": 75800.0,
                                                     "volume": 11130145
                                                },
                                                {
                                                      "stockCode": "005930",
                                                       "date": "2024-08-28",
                                                      "open": 75800.0,
                                                      "high": 76400.0,
                                                      "low": 75400.0,
                                                      "close": 76400.0,
                                                      "volume": 9794514
                                                },
                                                {
                                                      "stockCode": "005930",
                                                      "date": "2024-08-29",
                                                       "open": 73600.0,
                                                       "high": 74700.0,
                                                       "low": 73500.0,
                                                       "close": 74000.0,
                                                       "volume": 16884479
                                                }
                                            ]
                                        }
                                """)
                    }))
    })
    ResponseEntity<?> getStockHistory(@PathVariable String stockCode,
                                      @RequestParam StockPeriodType period);

    @TrackApi(description = "주식 분봉 차트 조회")
    @Operation(summary = "주식 분봉 차트 조회", description = "주식 분봉 차트를 조회하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주식 차트 조회 성공(당일/지난 일주일 응답값 필드 동일)",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                    {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": [
                                                 {
                                                     "stockCode": "900110",
                                                     "start": "2025-02-14 09:00",
                                                     "end": "2025-02-14 09:10",
                                                     "open": 53,
                                                     "close": 53,
                                                     "high": 53,
                                                     "low": 53,
                                                     "volume": 106707
                                                 },
                                                 {
                                                     "stockCode": "900110",
                                                     "start": "2025-02-14 09:10",
                                                     "end": "2025-02-14 09:20",
                                                     "open": 53,
                                                     "close": 53,
                                                     "high": 53,
                                                     "low": 53,
                                                     "volume": 106707
                                                 }
                                            ]
                                        }
                                """)
                    })),
            @ApiResponse(responseCode = "400", description = "당일 주식 분봉데이터 캐시 내 저장시간 이후 조회 시 실패",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                    {
                                        "code": "2003",
                                        "message": "당일 주식 조회 가능 기간이 아닙니다."
                                    }
                                """)
                    })),
    })
    public ResponseEntity<?> getStockMinutesHistory(@PathVariable String stockCode,
                                                    @RequestParam StockMinutesPeriodDto period);
}
