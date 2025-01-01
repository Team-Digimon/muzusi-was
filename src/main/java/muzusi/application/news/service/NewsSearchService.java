package muzusi.application.news.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.news.dto.NewsInfoDto;
import muzusi.domain.post.service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsSearchService {
    private final NewsService newsService;

    @Transactional(readOnly = true)
    public Page<NewsInfoDto> getAllNews(Pageable pageable) {
        return newsService.readAll(pageable)
                .map(NewsInfoDto::from);
    }

    @Transactional(readOnly = true)
    public Page<NewsInfoDto> searchNewsByKeyword(String keyword, Pageable pageable) {
        return newsService.readByKeyword(keyword, pageable)
                .map(NewsInfoDto::from);
    }
}