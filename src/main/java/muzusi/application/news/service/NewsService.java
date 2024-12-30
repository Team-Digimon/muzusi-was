package muzusi.application.news.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.post.entity.Post;
import muzusi.domain.post.service.PostService;
import muzusi.infrastructure.news.NewsApiClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsApiClient newsApiClient;
    private final PostService postService;

    private final static List<String> keywords = List.of("금융", "증권");

    @Transactional
    public void searchNewsByKeyword() {
        keywords.forEach(keyword ->
                newsApiClient.fetchNews(keyword).stream()
                        .filter(content -> !postService.existsByTitle(content.get("title")))
                        .map(content ->
                                Post.builder()
                                        .title(content.get("title"))
                                        .link(content.get("link"))
                                        .keyword(keyword)
                                        .pubDate(content.get("pubDate"))
                                        .build()
                        )
                        .forEach(postService::save)
        );
    }
}
