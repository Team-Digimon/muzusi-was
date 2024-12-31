package muzusi.domain.post.repository;

import muzusi.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    boolean existsByTitle(String title);
}
