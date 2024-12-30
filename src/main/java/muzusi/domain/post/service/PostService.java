package muzusi.domain.post.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.post.entity.Post;
import muzusi.domain.post.repository.PostRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public void save(Post post) {
        postRepository.save(post);
    }

    public boolean existsByTitle(String title) {
        return postRepository.existsByTitle(title);
    }
}
