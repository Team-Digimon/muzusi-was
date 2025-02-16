package muzusi.presentation.holding.api;

import api.link.checker.annotation.ApiGroup;
import api.link.checker.annotation.TrackApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import muzusi.global.security.auth.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@ApiGroup(value = "[보유 주식 API]")
@Tag(name = "[보유 주식 API]", description = "보유 주식 관련 API")
public interface HoldingApi {

    @TrackApi(description = "사용자 보유 주식 불러오기")
    @Operation(summary = "사용자 보유 주식 불러오기", description = "사용자 보유 주식을 불러오는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "보유 주식 불러오기 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": [
                                                {
                                                    "id": 1,
                                                    "stockName": "NAVER",
                                                    "stockCode": "035420",
                                                    "stockCount": 1,
                                                    "averagePrice": 200000,
                                                    "holdingAt": "2025-02-16T19:10:21.744044",
                                                    "rateOfReturn": 25.0,
                                                    "totalProfitAmount": 50000
                                                },
                                                {
                                                    "id": 2,
                                                    "stockName": "삼성전자",
                                                    "stockCode": "005930",
                                                    "stockCount": 50,
                                                    "averagePrice": 60000,
                                                    "holdingAt": "2025-02-16T19:10:21.744044",
                                                    "rateOfReturn": -25.0,
                                                    "totalProfitAmount": -500000
                                                }
                                            ]
                                        }
                                    """)
                    }))
    })
    ResponseEntity<?> getHoldings(@AuthenticationPrincipal CustomUserDetails userDetails);
}
