package muzusi.domain.post.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.post.entity.News;
import muzusi.domain.post.repository.NewsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;

    public void save(News post) {
        newsRepository.save(post);
    }

    public Page<News> readAll(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    public Page<News> readByKeyword(String keyword, Pageable pageable) {
        return newsRepository.findByKeyword(keyword, pageable);
    }

    public boolean existsByTitle(String title) {
        return newsRepository.existsByTitle(title);
    }
}
