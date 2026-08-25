package muzusi.application.news.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.news.port.FetchNewsPort;
import muzusi.domain.news.entity.News;
import muzusi.domain.news.service.NewsService;
import muzusi.domain.news.type.KeywordType;
import muzusi.global.util.datetime.DateTimeFormatterUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NewsManagementService {
    private final FetchNewsPort fetchNewsPort;
    private final NewsService newsService;

    /**
     * 뉴스 API 호출 및 저장 메서드
     * 키워드에 맞게 뉴스 API를 호출한 후, DB에 존재 여부를 확인하고 저장을 진행한다.
     */
    @Transactional
    public void createPostsFromNews() {
        List<News> recentNews = new ArrayList<>();
        Set<String> addedNewsLink = new HashSet<>();
        
        Arrays.stream(KeywordType.values()).forEach(
                keyword -> {
                    List<Map<String, String>> newsItems = fetchNewsPort.getNews(keyword.getName());
                    
                    newsItems.stream()
                            .filter(news -> !newsService.existsByLink(news.get("link")))
                            .filter(news -> addedNewsLink.add(news.get("link")))
                            .map(news -> News.builder()
                                    .title(news.get("title"))
                                    .link(news.get("link"))
                                    .keyword(keyword.getName())
                                    .pubDate(DateTimeFormatterUtil.parseToLocalDateTime(news.get("pubDate")))
                                    .build())
                            .forEach(recentNews::add);
                }
        );
        
        newsService.addAll(recentNews);
    }
    
    /**
     * 오래된 뉴스를 삭제하는 메서드.
     * 1일이 지난 뉴스들을 삭제한다.
     */
    @Transactional
    public void deleteNews() {
        LocalDateTime dateTime = LocalDateTime.now().minusDays(1);
        newsService.deleteByDateTime(dateTime);
    }
}
