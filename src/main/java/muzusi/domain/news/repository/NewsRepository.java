package muzusi.domain.news.repository;

import muzusi.domain.news.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    Page<News> findByKeyword(String keyword, Pageable pageable);
    boolean existsByLink(String link);

    @Query("SELECT n.id FROM news n WHERE n.pubDate < :dateTime")
    List<Long> findIdsByPubDateBefore(@Param("dateTime") LocalDateTime dateTime);

    @Modifying
    @Query("DELETE FROM news n WHERE n.id IN :ids")
    void deleteByIds(@Param("ids") List<Long> ids);
}
