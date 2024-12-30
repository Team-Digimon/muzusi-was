package muzusi.application.news.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.news.service.NewsService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class NewsScheduler {
    private final NewsService newsService;

    @Scheduled(cron = "0 */30 7-22 * * *")
    public void runDailyNewsProcessJob() {
        newsService.createPostsFromNews();
    }
}
