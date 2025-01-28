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
            @ApiResponse(responseCode = "400", description = "계좌 생성 횟수 제한",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": "4003",
                                            "message": "오늘은 이미 계좌를 생성했습니다."
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
}
