package muzusi.application.news.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.news.entity.News;
import muzusi.domain.news.service.NewsService;
import muzusi.global.util.datetime.DateTimeFormatterUtil;
import muzusi.infrastructure.news.NewsApiClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsManagementService {
    private final NewsApiClient newsApiClient;
    private final NewsService newsService;

    private final static List<String> keywords = List.of("코스피", "코스닥");

    /**
     * 뉴스 API 호출 및 저장 메서드
     * 키워드에 맞게 뉴스 API를 호출한 후, DB에 존재 여부를 확인하고 저장을 진행한다.
     */
    @Transactional
    public void createPostsFromNews() {
        keywords.forEach(keyword ->
                newsApiClient.fetchNews(keyword).stream()
                        .filter(content -> !newsService.existsByTitle(content.get("title")))
                        .map(content ->
                                News.builder()
                                        .title(content.get("title"))
                                        .link(content.get("link"))
                                        .keyword(keyword)
                                        .pubDate(DateTimeFormatterUtil.parseToLocalDateTime(content.get("pubDate")))
                                        .build()
                        )
                        .forEach(newsService::save)
        );
    }

    /**
     * 오래된 뉴스를 삭제하는 메서드.
     * 2일이 지난 뉴스들을 삭제한다.
     */
    @Transactional
    public void deleteNews() {
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        List<Long> newsIds = newsService.readIdsByDate(twoDaysAgo);
        newsService.deleteIds(newsIds);
    }
}
