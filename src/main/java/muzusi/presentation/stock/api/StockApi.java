package muzusi.presentation.stock.api;

import api.link.checker.annotation.ApiGroup;
import api.link.checker.annotation.TrackApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@ApiGroup(value = "[주식 API]")
@Tag(name = "[주식 API]", description = "주식 관련 API")
public interface StockApi {

    @TrackApi(description = "주식 검색어 자동완성")
    @Operation(summary = "주식 검색어 자동완성", description = "주식 검색어 자동완성하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주식 검색어 자동완성",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                    {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": [
                                                {
                                                     "stockCode": "035420",
                                                     "stockName": "네이버"
                                                },
                                                {
                                                     "stockCode": "005930",
                                                     "stockName": "삼성전자"
                                                }
                                            ]
                                        }
                                """)
                    }))
    })
    ResponseEntity<?> searchStock(@RequestParam String keyword);
}