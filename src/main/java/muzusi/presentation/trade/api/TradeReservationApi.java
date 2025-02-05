package muzusi.presentation.trade.api;

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

@ApiGroup(value = "[예약 내역 API]")
@Tag(name = "[예약 내역 API]", description = "예약 내역 관련 API")
public interface TradeReservationApi {

    @TrackApi(description = "예약 내역 불러오기")
    @Operation(summary = "예약 내역 불러오기", description = "예약 내을를 불러오는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 내역 불러오기 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": {
                                                "content": [
                                                    {
                                                        "id": "67a34d7f69fde23d0c0bef59",
                                                        "userId": 22,
                                                        "inputPrice": 55000,
                                                        "stockCount": 10,
                                                        "stockName": "삼성전자",
                                                        "stockCode": "005930",
                                                        "tradeType": "BUY",
                                                        "createdAt": "2025-02-05T14:37:35.249"
                                                    },
                                                    {
                                                        "id": "67a34d7f69fde23d0c0bef59",
                                                        "userId": 22,
                                                        "inputPrice": 250000,
                                                        "stockCount": 10,
                                                        "stockName": "NAVER",
                                                        "stockCode": "035420",
                                                        "tradeType": "SELL",
                                                        "createdAt": "2025-02-05T12:32:30.582"
                                                    }
                                                ],
                                                "page": {
                                                    "size": 10,
                                                    "number": 0,
                                                    "totalElements": 2,
                                                    "totalPages": 1
                                                }
                                            }
                                        }
                                    """)
                    }))
    })
    ResponseEntity<?> getAllReservations(@AuthenticationPrincipal CustomUserDetails userDetails);
}
