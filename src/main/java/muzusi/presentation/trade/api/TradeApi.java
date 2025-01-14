package muzusi.presentation.trade.api;

import api.link.checker.annotation.ApiGroup;
import api.link.checker.annotation.TrackApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import muzusi.application.trade.dto.TradeReqDto;
import muzusi.global.security.auth.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@ApiGroup(value = "[거래 API]")
@Tag(name = "[거래 API]", description = "거래 관련 API")
public interface TradeApi {

    @TrackApi(description = "주식 거래")
    @Operation(summary = "주식 거래", description = "주식 매수/매도를 위한 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주식 거래 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다."
                                        }
                                    """)
                    })),
            @ApiResponse(responseCode = "404", description = "주식 존재 x ",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "NotFoundStock", value = """
                                        {
                                            "code": "2001",
                                            "message": "주식 종목이 존재하지 않습니다."
                                        }
                                    """),
                            @ExampleObject(name = "NotFoundAccount", value = """
                                        {
                                            "code": "4001",
                                            "message": "계좌가 존재하지 않습니다."
                                        }
                                    """)
                    })),
            @ApiResponse(responseCode = "422", description = "유효성 검사 실패",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "stockPrice": "주식 가격은 필수 입력입니다.",
                                            "tradeType": "매수/매도는 필수 선택입니다.",
                                            "stockCode": "주식 코드는 필수 정보입니다.",
                                            "stockCount": "주식 개수는 필수 입력입니다."
                                        }
                                    """)
                    })),
    })
    ResponseEntity<?> tradeStock(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @RequestBody TradeReqDto tradeReqDto);
}
