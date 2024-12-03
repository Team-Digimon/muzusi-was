package muzusi.domain.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String publisher;

    private String image;

    @Column(name = "target_url")
    private String targetUrl;

    @Builder
    public Post(String title, String publisher, String image, String targetUrl) {
        this.title = title;
        this.publisher = publisher;
        this.image = image;
        this.targetUrl = targetUrl;
    }
}
