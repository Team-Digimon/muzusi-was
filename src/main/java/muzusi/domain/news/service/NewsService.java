package muzusi.domain.news.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.news.entity.News;
import muzusi.domain.news.repository.NewsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;

    public void save(News news) {
        newsRepository.save(news);
    }

    public Page<News> readAll(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    public Page<News> readByKeyword(String keyword, Pageable pageable) {
        return newsRepository.findByKeyword(keyword, pageable);
    }

    public List<Long> readIdsByDate(LocalDateTime dateTime) {
        return newsRepository.findIdsByPubDateBefore(dateTime);
    }

    public boolean existsByLink(String link) {
        return newsRepository.existsByLink(link);
    }

    public void deleteByIds(List<Long> ids) {
        newsRepository.deleteByIds(ids);
    }
}
