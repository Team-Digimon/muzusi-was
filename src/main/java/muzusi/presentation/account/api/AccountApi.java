package muzusi.presentation.account.api;

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
import org.springframework.web.bind.annotation.PathVariable;

@ApiGroup(value = "[계좌 API]")
@Tag(name = "[계좌 API]", description = "계좌 관련 API")
public interface AccountApi {

    @TrackApi(description = "계좌 생성")
    @Operation(summary = "계좌 생성", description = "시드 재생성을 위한 계좌 생성하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "계좌생성 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다."
                                        }
                                    """)
                    })),
            @ApiResponse(responseCode = "400", description = "계좌 생성 오류",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "AccountCreationLimit", value = """
                                        {
                                            "code": "4003",
                                            "message": "오늘은 이미 계좌를 생성했습니다."
                                        }
                                    """),
                            @ExampleObject(name = "AccountCreationTimeLimit", value = """
                                        {
                                            "code": "4005",
                                            "message": "오전 9시 이전에만 계좌를 개설할 수 있습니다."
                                        }
                                    """)
                    }))
    })
    ResponseEntity<?> createNewAccount(@AuthenticationPrincipal CustomUserDetails userDetails);

    @TrackApi(description = "사용자 전체 계좌 불러오기")
    @Operation(summary = "사용자 전체 계좌 불러오기", description = "사용자 전체 계좌를 불러오는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "계좌 불러오기 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": {
                                                "content": [
                                                    {
                                                        "id": 1,
                                                        "totalBalance": 10000000,
                                                        "reservedPrice": 300000
                                                    },
                                                    {
                                                        "id": 2,
                                                        "totalBalance": 20000000,
                                                        "reservedPrice": 300000
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
    ResponseEntity<?> getAllAccounts(@AuthenticationPrincipal CustomUserDetails userDetails);

    @TrackApi(description = "사용자 최신 계좌 불러오기")
    @Operation(summary = "사용자 최신 계좌 불러오기", description = "사용자 최신 계좌를 불러오는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "계좌 불러오기 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": {
                                                "id": 2,
                                                "totalBalance": 20000000,
                                                "reservedPrice": 300000
                                            }
                                        }
                                    """)
                    }))
    })
    ResponseEntity<?> getAccount(@AuthenticationPrincipal CustomUserDetails userDetails);

    @TrackApi(description = "계좌 거래 내역 불러오기")
    @Operation(summary = "계좌 거래 내역 불러오기", description = "계좌 거래내역을 불러오는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거래내역 호출 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": [
                                                {
                                                    "id": 1,
                                                    "stockPrice": 60000,
                                                    "stockCount": 10,
                                                    "stockName": "삼성전자",
                                                    "stockCode": "005930",
                                                    "tradeType": "BUY",
                                                    "tradeAt": "2025-01-28T21:15:22.81816"
                                                },
                                                {
                                                    "id": 2,
                                                    "stockPrice": 65000,
                                                    "stockCount": 10,
                                                    "stockName": "삼성전자",
                                                    "stockCode": "005930",
                                                    "tradeType": "SELL",
                                                    "tradeAt": "2025-01-28T21:15:37.963513"
                                                }
                                            ]
                                        }
                                    """)
                    })),
            @ApiResponse(responseCode = "404", description = "계좌 존재하지 않음",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": "4001",
                                            "message": "계좌가 존재하지 않습니다."
                                        }
                                    """)
                    })),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": "4004",
                                            "message": "해당 계좌에 접근 권한이 없습니다."
                                        }
                                    """)
                    })),
    })
    ResponseEntity<?> getTradesByAccountId(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable Long accountId);
}
