package muzusi.presentation.news.controller;

import lombok.RequiredArgsConstructor;
import muzusi.application.news.service.NewsSearchService;
import muzusi.global.response.success.SuccessResponse;
import muzusi.presentation.news.api.NewsApi;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/news")
@RequiredArgsConstructor
public class NewsController implements NewsApi {
    private final NewsSearchService newsSearchService;

    @Override
    @GetMapping
    public ResponseEntity<?> getAllNews(
            @PageableDefault(sort = "pubDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(SuccessResponse.from(newsSearchService.getAllNews(pageable)));
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<?> searchNewsByKeyword(
            @PageableDefault(sort = "pubDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(name = "keyword") String keyword
    ) {
        return ResponseEntity.ok(SuccessResponse.from(newsSearchService.searchNewsByKeyword(keyword, pageable)));
    }
}
