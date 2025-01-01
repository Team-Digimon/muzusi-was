package muzusi.presentation.news.api;

import api.link.checker.annotation.ApiGroup;
import api.link.checker.annotation.TrackApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@ApiGroup(value = "[뉴스 API]")
@Tag(name = "[뉴스 API]", description = "뉴스 관련 API")
public interface NewsApi {

    @TrackApi(description = "뉴스 불러오기")
    @Operation(summary = "뉴스 불러오기", description = "뉴스를 불러오는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "뉴스 불러오기 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": {
                                                "content": [
                                                    {
                                                        "title": "뉴스 1",
                                                        "link": "http://news1.example.com",
                                                        "keyword": "코스피",
                                                        "pubDate": "2025-01-02T14:30:00"
                                                    },
                                                    {
                                                        "title": "뉴스 2",
                                                        "link": "http://news2.example.com",
                                                        "keyword": "코스닥",
                                                        "pubDate": "2025-01-02T14:30:01"
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
    ResponseEntity<?> getAllNews(
            @PageableDefault(sort = "pubDate", direction = Sort.Direction.DESC) Pageable pageable
    );

    @TrackApi(description = "키워드로 뉴스 불러오기")
    @Operation(summary = "키워드로 뉴스 불러오기", description = "키워드(코스피, 코스닥)로 뉴스를 불러오는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "뉴스 불러오기 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": {
                                                "content": [
                                                    {
                                                        "title": "뉴스 1",
                                                        "link": "http://news1.example.com",
                                                        "keyword": "코스피",
                                                        "pubDate": "2025-01-02T14:30:00"
                                                    },
                                                    {
                                                        "title": "뉴스 2",
                                                        "link": "http://news2.example.com",
                                                        "keyword": "코스피",
                                                        "pubDate": "2025-01-02T14:30:01"
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
    ResponseEntity<?> searchNewsByKeyword(
            @PageableDefault(sort = "pubDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(name = "keyword") String keyword
    );
}
