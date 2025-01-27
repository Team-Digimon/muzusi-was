package muzusi.presentation.stock.api;

import api.link.checker.annotation.ApiGroup;
import api.link.checker.annotation.TrackApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
}
