package muzusi.application.news.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.post.entity.Post;
import muzusi.domain.post.service.PostService;
import muzusi.global.util.datetime.DateTimeFormatterUtil;
import muzusi.infrastructure.news.NewsApiClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsManagementService {
    private final NewsApiClient newsApiClient;
    private final PostService postService;

    private final static List<String> keywords = List.of("코스피", "코스닥");

    /**
     * 뉴스 API 호출 및 저장 메서드
     * 키워드에 맞게 뉴스 API를 호출한 후, DB에 존재 여부를 확인하고 저장을 진행한다.
     */
    @Transactional
    public void createPostsFromNews() {
        keywords.forEach(keyword ->
                newsApiClient.fetchNews(keyword).stream()
                        .filter(content -> !postService.existsByTitle(content.get("title")))
                        .map(content ->
                                Post.builder()
                                        .title(content.get("title"))
                                        .link(content.get("link"))
                                        .keyword(keyword)
                                        .pubDate(DateTimeFormatterUtil.parseToLocalDateTime(content.get("pubDate")))
                                        .build()
                        )
                        .forEach(postService::save)
        );
    }
}
