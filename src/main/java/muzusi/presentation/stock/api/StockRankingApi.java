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
                    content = @Content(examples = {
                            @ExampleObject(value = """
                                    {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": [
                                                "rank": [
                                                             {
                                                                 "name": "데이원컴퍼니",
                                                                 "code": "373160",
                                                                 "rank": 1,
                                                                 "price": 7800,
                                                                 "prdyVrss": -5200,
                                                                 "prdyCtrt": -40.0,
                                                                 "avrgVol": 5373704
                                                             },
                                                             {
                                                                 "name": "와이즈넛",
                                                                 "code": "096250",
                                                                 "rank": 2,
                                                                 "price": 10800,
                                                                 "prdyVrss": -6200,
                                                                 "prdyCtrt": -36.47,
                                                                 "avrgVol": 3254006
                                                             },
                                                             {
                                                                 "name": "스타코링크",
                                                                 "code": "060240",
                                                                 "rank": 3,
                                                                 "price": 376,
                                                                 "prdyVrss": -125,
                                                                 "prdyCtrt": -24.95,
                                                                 "avrgVol": 2410117
                                                             },
                                                             {
                                                                 "name": "대영포장",
                                                                 "code": "014160",
                                                                 "rank": 4,
                                                                 "price": 1740,
                                                                 "prdyVrss": -505,
                                                                 "prdyCtrt": -22.49,
                                                                 "avrgVol": 56059534
                                                             },
                                                             {
                                                                 "name": "세원물산",
                                                                 "code": "024830",
                                                                 "rank": 5,
                                                                 "price": 10380,
                                                                 "prdyVrss": -2420,
                                                                 "prdyCtrt": -18.91,
                                                                 "avrgVol": 452062
                                                             },
                                                             {
                                                                 "name": "미트박스",
                                                                 "code": "475460",
                                                                 "rank": 6,
                                                                 "price": 12290,
                                                                 "prdyVrss": -1910,
                                                                 "prdyCtrt": -13.45,
                                                                 "avrgVol": 2134805
                                                             },
                                                             {
                                                                 "name": "원풍물산",
                                                                 "code": "008290",
                                                                 "rank": 7,
                                                                 "price": 499,
                                                                 "prdyVrss": -76,
                                                                 "prdyCtrt": -13.22,
                                                                 "avrgVol": 6049577
                                                             },
                                                             {
                                                                 "name": "우원개발",
                                                                 "code": "046940",
                                                                 "rank": 8,
                                                                 "price": 3155,
                                                                 "prdyVrss": -470,
                                                                 "prdyCtrt": -12.97,
                                                                 "avrgVol": 1584766
                                                             },
                                                             {
                                                                 "name": "사조오양",
                                                                 "code": "006090",
                                                                 "rank": 9,
                                                                 "price": 8740,
                                                                 "prdyVrss": -1190,
                                                                 "prdyCtrt": -11.98,
                                                                 "avrgVol": 237785
                                                             },
                                                             {
                                                                 "name": "케이씨피드",
                                                                 "code": "025880",
                                                                 "rank": 10,
                                                                 "price": 2970,
                                                                 "prdyVrss": -395,
                                                                 "prdyCtrt": -11.74,
                                                                 "avrgVol": 1103958
                                                             },
                                                             {
                                                                 "name": "체리부로",
                                                                 "code": "066360",
                                                                 "rank": 11,
                                                                 "price": 923,
                                                                 "prdyVrss": -118,
                                                                 "prdyCtrt": -11.34,
                                                                 "avrgVol": 1881878
                                                             },
                                                             {
                                                                 "name": "화신정공",
                                                                 "code": "126640",
                                                                 "rank": 12,
                                                                 "price": 1659,
                                                                 "prdyVrss": -206,
                                                                 "prdyCtrt": -11.05,
                                                                 "avrgVol": 2057336
                                                             },
                                                             {
                                                                 "name": "경동나비엔",
                                                                 "code": "009450",
                                                                 "rank": 13,
                                                                 "price": 90500,
                                                                 "prdyVrss": -10600,
                                                                 "prdyCtrt": -10.48,
                                                                 "avrgVol": 251738
                                                             },
                                                             {
                                                                 "name": "베셀",
                                                                 "code": "177350",
                                                                 "rank": 14,
                                                                 "price": 1126,
                                                                 "prdyVrss": -122,
                                                                 "prdyCtrt": -9.78,
                                                                 "avrgVol": 1363326
                                                             },
                                                             {
                                                                 "name": "평화홀딩스",
                                                                 "code": "010770",
                                                                 "rank": 15,
                                                                 "price": 3590,
                                                                 "prdyVrss": -375,
                                                                 "prdyCtrt": -9.46,
                                                                 "avrgVol": 4780459
                                                             },
                                                             {
                                                                 "name": "진영",
                                                                 "code": "285800",
                                                                 "rank": 16,
                                                                 "price": 2960,
                                                                 "prdyVrss": -305,
                                                                 "prdyCtrt": -9.34,
                                                                 "avrgVol": 690875
                                                             },
                                                             {
                                                                 "name": "SNT모티브",
                                                                 "code": "064960",
                                                                 "rank": 17,
                                                                 "price": 25500,
                                                                 "prdyVrss": -2600,
                                                                 "prdyCtrt": -9.25,
                                                                 "avrgVol": 214020
                                                             },
                                                             {
                                                                 "name": "한솔홈데코",
                                                                 "code": "025750",
                                                                 "rank": 18,
                                                                 "price": 1075,
                                                                 "prdyVrss": -105,
                                                                 "prdyCtrt": -8.9,
                                                                 "avrgVol": 10178917
                                                             },
                                                             {
                                                                 "name": "옵티시스",
                                                                 "code": "109080",
                                                                 "rank": 19,
                                                                 "price": 10300,
                                                                 "prdyVrss": -990,
                                                                 "prdyCtrt": -8.77,
                                                                 "avrgVol": 180310
                                                             },
                                                             {
                                                                 "name": "옵티코어",
                                                                 "code": "380540",
                                                                 "rank": 20,
                                                                 "price": 1205,
                                                                 "prdyVrss": -106,
                                                                 "prdyCtrt": -8.09,
                                                                 "avrgVol": 529840
                                                             },
                                                             {
                                                                 "name": "아시아경제",
                                                                 "code": "127710",
                                                                 "rank": 21,
                                                                 "price": 1506,
                                                                 "prdyVrss": -131,
                                                                 "prdyCtrt": -8.0,
                                                                 "avrgVol": 62189
                                                             },
                                                             {
                                                                 "name": "하이퍼코퍼레이션",
                                                                 "code": "065650",
                                                                 "rank": 22,
                                                                 "price": 1098,
                                                                 "prdyVrss": -94,
                                                                 "prdyCtrt": -7.89,
                                                                 "avrgVol": 1148543
                                                             },
                                                             {
                                                                 "name": "아이윈플러스",
                                                                 "code": "123010",
                                                                 "rank": 23,
                                                                 "price": 1520,
                                                                 "prdyVrss": -130,
                                                                 "prdyCtrt": -7.88,
                                                                 "avrgVol": 4564589
                                                             },
                                                             {
                                                                 "name": "원티드랩",
                                                                 "code": "376980",
                                                                 "rank": 24,
                                                                 "price": 7300,
                                                                 "prdyVrss": -600,
                                                                 "prdyCtrt": -7.59,
                                                                 "avrgVol": 211252
                                                             },
                                                             {
                                                                 "name": "삼성 인버스 2X 항셍테크 ETN(H) B",
                                                                 "code": "Q530122",
                                                                 "rank": 25,
                                                                 "price": 10030,
                                                                 "prdyVrss": -775,
                                                                 "prdyCtrt": -7.17,
                                                                 "avrgVol": 19725
                                                             },
                                                             {
                                                                 "name": "KB 인버스 2X 항셍테크 선물 ETN",
                                                                 "code": "Q580019",
                                                                 "rank": 26,
                                                                 "price": 6350,
                                                                 "prdyVrss": -475,
                                                                 "prdyCtrt": -6.96,
                                                                 "avrgVol": 20098
                                                             },
                                                             {
                                                                 "name": "큐라티스",
                                                                 "code": "348080",
                                                                 "rank": 27,
                                                                 "price": 633,
                                                                 "prdyVrss": -47,
                                                                 "prdyCtrt": -6.91,
                                                                 "avrgVol": 642559
                                                             },
                                                             {
                                                                 "name": "젠큐릭스",
                                                                 "code": "229000",
                                                                 "rank": 28,
                                                                 "price": 1878,
                                                                 "prdyVrss": -137,
                                                                 "prdyCtrt": -6.8,
                                                                 "avrgVol": 312885
                                                             },
                                                             {
                                                                 "name": "오킨스전자",
                                                                 "code": "080580",
                                                                 "rank": 29,
                                                                 "price": 5830,
                                                                 "prdyVrss": -420,
                                                                 "prdyCtrt": -6.72,
                                                                 "avrgVol": 496736
                                                             },
                                                             {
                                                                 "name": "한국첨단소재",
                                                                 "code": "062970",
                                                                 "rank": 30,
                                                                 "price": 6400,
                                                                 "prdyVrss": -460,
                                                                 "prdyCtrt": -6.71,
                                                                 "avrgVol": 6561625
                                                             }
                                                         ],
                                                         "time": "2025-01-27 03:02:21"
                                            ]
                                        }
                                """)
                    }))
    })
    public ResponseEntity<?> getStockRanking(@RequestParam StockRankingType type);
}