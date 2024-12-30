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

    private String link;

    private String keyword;

    @Column(name = "pub_date")
    private String pubDate;

    @Builder
    public Post(String title, String link, String keyword, String pubDate) {
        this.title = title;
        this.link = link;
        this.keyword = keyword;
        this.pubDate = pubDate;
    }
}
