package muzusi.application.news.dto;

import muzusi.domain.news.entity.News;

import java.time.LocalDateTime;

public record NewsInfoDto(
        Long id,
        String title,
        String link,
        String keyword,
        LocalDateTime pubDate
) {
    public static NewsInfoDto from(News news) {
        return new NewsInfoDto(
                news.getId(),
                news.getTitle(),
                news.getLink(),
                news.getKeyword(),
                news.getPubDate()
        );
    }
}
