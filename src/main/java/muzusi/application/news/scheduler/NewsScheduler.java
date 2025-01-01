package muzusi.application.news.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.news.service.NewsManagementService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class NewsScheduler {
    private final NewsManagementService newsManagementService;

    @Scheduled(cron = "0 */10 7-22 * * *")
    public void runDailyNewsProcessJob() {
        newsManagementService.createPostsFromNews();
    }
}
