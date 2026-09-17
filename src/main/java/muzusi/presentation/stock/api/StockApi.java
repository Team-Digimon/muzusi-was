package muzusi.presentation.stock.api;

import api.link.checker.annotation.ApiGroup;
import api.link.checker.annotation.TrackApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@ApiGroup(value = "[주식 기본 정보 조회 API]")
@Tag(name = "[주식 기본 정보 조회 API]", description = "주식 기본 정보 조회 관련 API")
public interface StockApi {
    @TrackApi(description = "주식 기본 정보 조회")
    @Operation(summary = "주식 기본 정보 조회", description = "주식 코드로 종목코드, 종목명, 시장구분을 조회하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주식 기본 정보 조회 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                                "code": 200,
                                                "message": "요청이 성공하였습니다.",
                                                "data": {
                                                    "stockCode": "005930",
                                                    "stockName": "삼성전자",
                                                    "marketType": "KOSPI"
                                                }
                                            }
                                    """)
                    })),
            @ApiResponse(responseCode = "404", description = "Not Found 관련",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "NotFoundStock", value = """
                                        {
                                            "code": "2001",
                                            "message": "주식 종목이 존재하지 않습니다."
                                        }
                                    """)
                    }))
    })
    ResponseEntity<?> getStock(
            @Parameter(description = "주식 코드", required = true, example = "005930")
            @PathVariable(name = "stockCode") String stockCode
    );
}
