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
                    }))
    })
    ResponseEntity<?> createNewAccount(@AuthenticationPrincipal CustomUserDetails userDetails);
}
