package muzusi.presentation.stock.api;

import api.link.checker.annotation.ApiGroup;
import api.link.checker.annotation.TrackApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import muzusi.domain.stock.type.StockRankingType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@ApiGroup(value = "[주식 순위 API]")
@Tag(name = "[주식 순위 API]", description = "주식 순위 관련 API")
public interface StockRankingApi {

    @TrackApi(description = "주식 순위 조회")
    @Operation(summary = "주식 순위 조회", description = "주식 순위를 조회하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주식 순위 조회 성공(급상승/급하락/거래량 응답값 필드 동일)",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                    {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": [
                                                {
                                                    "name": "대원전선우",
                                                    "code": "006345",
                                                    "rank": 1,
                                                    "price": 4925,
                                                    "prdyVrss": 260,
                                                    "prdyCtrt": 5.57,
                                                    "avrgVol": 1205145
                                                },
                                                {
                                                    "name": "삼성전자우",
                                                    "code": "005935",
                                                    "rank": 2,
                                                    "price": 44000,
                                                    "prdyVrss": 250,
                                                    "prdyCtrt": 0.57,
                                                    "avrgVol": 875275
                                                },
                                                {
                                                    "name": "솔루스첨단소재2우B",
                                                    "code": "33637L",
                                                    "rank": 3,
                                                    "price": 5320,
                                                    "prdyVrss": -340,
                                                    "prdyCtrt": -6.01,
                                                    "avrgVol": 381572
                                                },
                                                {
                                                    "name": "솔루스첨단소재1우",
                                                    "code": "33637K",
                                                    "rank": 4,
                                                    "price": 2260,
                                                    "prdyVrss": -65,
                                                    "prdyCtrt": -2.8,
                                                    "avrgVol": 344969
                                                },
                                                {
                                                    "name": "미래에셋증권2우B",
                                                    "code": "00680K",
                                                    "rank": 5,
                                                    "price": 3950,
                                                    "prdyVrss": 15,
                                                    "prdyCtrt": 0.38,
                                                    "avrgVol": 93184
                                                },
                                                {
                                                    "name": "대신증권우",
                                                    "code": "003545",
                                                    "rank": 6,
                                                    "price": 15670,
                                                    "prdyVrss": 100,
                                                    "prdyCtrt": 0.64,
                                                    "avrgVol": 63402
                                                },
                                                {
                                                    "name": "대신증권2우B",
                                                    "code": "003547",
                                                    "rank": 7,
                                                    "price": 14860,
                                                    "prdyVrss": 50,
                                                    "prdyCtrt": 0.34,
                                                    "avrgVol": 58392
                                                },
                                                {
                                                    "name": "NH투자증권우",
                                                    "code": "005945",
                                                    "rank": 8,
                                                    "price": 12520,
                                                    "prdyVrss": 60,
                                                    "prdyCtrt": 0.48,
                                                    "avrgVol": 52088
                                                },
                                                {
                                                    "name": "현대차2우B",
                                                    "code": "005387",
                                                    "rank": 9,
                                                    "price": 159600,
                                                    "prdyVrss": -1200,
                                                    "prdyCtrt": -0.75,
                                                    "avrgVol": 45771
                                                },
                                                {
                                                    "name": "두산우",
                                                    "code": "000155",
                                                    "rank": 10,
                                                    "price": 142300,
                                                    "prdyVrss": 10700,
                                                    "prdyCtrt": 8.13,
                                                    "avrgVol": 44698
                                                },
                                                {
                                                    "name": "현대차우",
                                                    "code": "005385",
                                                    "rank": 11,
                                                    "price": 154800,
                                                    "prdyVrss": 400,
                                                    "prdyCtrt": 0.26,
                                                    "avrgVol": 37568
                                                },
                                                {
                                                    "name": "미래에셋증권우",
                                                    "code": "006805",
                                                    "rank": 12,
                                                    "price": 4385,
                                                    "prdyVrss": 35,
                                                    "prdyCtrt": 0.8,
                                                    "avrgVol": 32734
                                                },
                                                {
                                                    "name": "금호석유우",
                                                    "code": "011785",
                                                    "rank": 13,
                                                    "price": 52000,
                                                    "prdyVrss": 3500,
                                                    "prdyCtrt": 7.22,
                                                    "avrgVol": 31816
                                                },
                                                {
                                                    "name": "LG전자우",
                                                    "code": "066575",
                                                    "rank": 14,
                                                    "price": 40200,
                                                    "prdyVrss": -200,
                                                    "prdyCtrt": -0.5,
                                                    "avrgVol": 27920
                                                },
                                                {
                                                    "name": "코오롱모빌리티그룹우",
                                                    "code": "45014K",
                                                    "rank": 15,
                                                    "price": 6410,
                                                    "prdyVrss": 0,
                                                    "prdyCtrt": 0.0,
                                                    "avrgVol": 24223
                                                },
                                                {
                                                    "name": "한화투자증권우",
                                                    "code": "003535",
                                                    "rank": 16,
                                                    "price": 7040,
                                                    "prdyVrss": 90,
                                                    "prdyCtrt": 1.29,
                                                    "avrgVol": 23764
                                                },
                                                {
                                                    "name": "LG화학우",
                                                    "code": "051915",
                                                    "rank": 17,
                                                    "price": 153500,
                                                    "prdyVrss": -5600,
                                                    "prdyCtrt": -3.52,
                                                    "avrgVol": 18579
                                                },
                                                {
                                                    "name": "서울식품우",
                                                    "code": "004415",
                                                    "rank": 18,
                                                    "price": 1200,
                                                    "prdyVrss": -35,
                                                    "prdyCtrt": -2.83,
                                                    "avrgVol": 18492
                                                },
                                                {
                                                    "name": "한국금융지주우",
                                                    "code": "071055",
                                                    "rank": 19,
                                                    "price": 56000,
                                                    "prdyVrss": 1100,
                                                    "prdyCtrt": 2.0,
                                                    "avrgVol": 16998
                                                },
                                                {
                                                    "name": "한화솔루션우",
                                                    "code": "009835",
                                                    "rank": 20,
                                                    "price": 17420,
                                                    "prdyVrss": -870,
                                                    "prdyCtrt": -4.76,
                                                    "avrgVol": 15958
                                                },
                                                {
                                                    "name": "대덕전자1우",
                                                    "code": "35320K",
                                                    "rank": 21,
                                                    "price": 8280,
                                                    "prdyVrss": 150,
                                                    "prdyCtrt": 1.85,
                                                    "avrgVol": 14840
                                                },
                                                {
                                                    "name": "대한제당우",
                                                    "code": "001795",
                                                    "rank": 22,
                                                    "price": 2275,
                                                    "prdyVrss": 5,
                                                    "prdyCtrt": 0.22,
                                                    "avrgVol": 14184
                                                },
                                                {
                                                    "name": "한화3우B",
                                                    "code": "00088K",
                                                    "rank": 23,
                                                    "price": 15280,
                                                    "prdyVrss": 0,
                                                    "prdyCtrt": 0.0,
                                                    "avrgVol": 12970
                                                },
                                                {
                                                    "name": "덕성우",
                                                    "code": "004835",
                                                    "rank": 24,
                                                    "price": 9900,
                                                    "prdyVrss": -20,
                                                    "prdyCtrt": -0.2,
                                                    "avrgVol": 12053
                                                },
                                                {
                                                    "name": "대상우",
                                                    "code": "001685",
                                                    "rank": 25,
                                                    "price": 15540,
                                                    "prdyVrss": -90,
                                                    "prdyCtrt": -0.58,
                                                    "avrgVol": 11151
                                                },
                                                {
                                                    "name": "유한양행우",
                                                    "code": "000105",
                                                    "rank": 26,
                                                    "price": 118500,
                                                    "prdyVrss": 500,
                                                    "prdyCtrt": 0.42,
                                                    "avrgVol": 10607
                                                },
                                                {
                                                    "name": "태양금속우",
                                                    "code": "004105",
                                                    "rank": 27,
                                                    "price": 4700,
                                                    "prdyVrss": -30,
                                                    "prdyCtrt": -0.63,
                                                    "avrgVol": 9601
                                                },
                                                {
                                                    "name": "NPC우",
                                                    "code": "004255",
                                                    "rank": 28,
                                                    "price": 2505,
                                                    "prdyVrss": -10,
                                                    "prdyCtrt": -0.4,
                                                    "avrgVol": 9579
                                                },
                                                {
                                                    "name": "흥국화재우",
                                                    "code": "000545",
                                                    "rank": 29,
                                                    "price": 5120,
                                                    "prdyVrss": -20,
                                                    "prdyCtrt": -0.39,
                                                    "avrgVol": 9061
                                                },
                                                {
                                                    "name": "대상홀딩스우",
                                                    "code": "084695",
                                                    "rank": 30,
                                                    "price": 19360,
                                                    "prdyVrss": -630,
                                                    "prdyCtrt": -3.15,
                                                    "avrgVol": 7971
                                                }
                                            ]
                                        }
                                """)
                    }))
    })
    public ResponseEntity<?> getStockRanking(@RequestParam StockRankingType type);
}