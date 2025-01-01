package muzusi.domain.post.repository;

import muzusi.domain.post.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {
    Page<News> findByKeyword(String keyword, Pageable pageable);
    boolean existsByTitle(String title);
}
