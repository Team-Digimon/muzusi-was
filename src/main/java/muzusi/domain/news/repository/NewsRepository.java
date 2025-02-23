package muzusi.domain.news.repository;

import muzusi.domain.news.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NewsRepository extends JpaRepository<News, Long> {
    Page<News> findByKeyword(String keyword, Pageable pageable);
    boolean existsByLink(String link);

    @Modifying
    @Query("DELETE FROM news n WHERE n.pubDate < :dateTime")
    void deleteByDateTimeBefore(@Param("dateTime") LocalDateTime dateTime);
}
