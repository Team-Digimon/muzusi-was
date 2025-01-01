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

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "news")
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String link;

    private String keyword;

    @Column(name = "pub_date")
    private LocalDateTime pubDate;

    @Builder
    public News(String title, String link, String keyword, LocalDateTime pubDate) {
        this.title = title;
        this.link = link;
        this.keyword = keyword;
        this.pubDate = pubDate;
    }
}
