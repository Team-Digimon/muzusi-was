package muzusi.application.news.service;

import muzusi.application.news.port.FetchNewsPort;
import muzusi.domain.news.entity.News;
import muzusi.domain.news.service.NewsService;
import muzusi.domain.news.type.KeywordType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsManagementServiceTest {
    @Mock
    private FetchNewsPort fetchNewsPort;

    @Mock
    private NewsService newsService;

    @InjectMocks
    private NewsManagementService newsManagementService;

    @Nested
    @DisplayName("뉴스 수집 및 저장")
    class CreatePostsFromNews {
        @Test
        @DisplayName("이미 DB에 존재하는 링크의 뉴스는 저장 대상에서 제외한다")
        void excludeNewsAlreadyExistsInDb() {
            // given
            Map<String, String> existingNews = Map.of(
                    "title", "이미 존재하는 뉴스",
                    "link", "https://news.test/existing",
                    "pubDate", "Mon, 30 Dec 2024 20:14:00 +0900"
            );
            when(fetchNewsPort.getNews(KeywordType.KOSPI.getName())).thenReturn(List.of(existingNews));
            when(fetchNewsPort.getNews(KeywordType.KOSDAQ.getName())).thenReturn(List.of());
            when(newsService.existsByLink("https://news.test/existing")).thenReturn(true);

            // when
            newsManagementService.createPostsFromNews();

            // then
            ArgumentCaptor<List<News>> captor = ArgumentCaptor.forClass(List.class);
            verify(newsService).addAll(captor.capture());
            assertThat(captor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("서로 다른 키워드 조회 결과에 동일한 링크가 있으면 한 번만 저장한다")
        void excludeDuplicatedLinkAcrossKeywords() {
            // given
            Map<String, String> duplicatedNews = Map.of(
                    "title", "코스피/코스닥 공통 뉴스",
                    "link", "https://news.test/duplicated",
                    "pubDate", "Mon, 30 Dec 2024 20:14:00 +0900"
            );
            when(fetchNewsPort.getNews(KeywordType.KOSPI.getName())).thenReturn(List.of(duplicatedNews));
            when(fetchNewsPort.getNews(KeywordType.KOSDAQ.getName())).thenReturn(List.of(duplicatedNews));
            when(newsService.existsByLink("https://news.test/duplicated")).thenReturn(false);

            // when
            newsManagementService.createPostsFromNews();

            // then
            ArgumentCaptor<List<News>> captor = ArgumentCaptor.forClass(List.class);
            verify(newsService).addAll(captor.capture());
            assertThat(captor.getValue())
                    .extracting(News::getLink)
                    .containsExactly("https://news.test/duplicated");
        }

        @Test
        @DisplayName("DB에도 없고 중복도 아닌 신규 뉴스는 키워드 정보와 함께 저장한다")
        void saveNewNewsWithKeyword() {
            // given
            Map<String, String> kospiNews = Map.of(
                    "title", "코스피 뉴스",
                    "link", "https://news.test/kospi",
                    "pubDate", "Mon, 30 Dec 2024 20:14:00 +0900"
            );
            Map<String, String> kosdaqNews = Map.of(
                    "title", "코스닥 뉴스",
                    "link", "https://news.test/kosdaq",
                    "pubDate", "Tue, 31 Dec 2024 09:00:00 +0900"
            );
            when(fetchNewsPort.getNews(KeywordType.KOSPI.getName())).thenReturn(List.of(kospiNews));
            when(fetchNewsPort.getNews(KeywordType.KOSDAQ.getName())).thenReturn(List.of(kosdaqNews));
            when(newsService.existsByLink(any())).thenReturn(false);

            // when
            newsManagementService.createPostsFromNews();

            // then
            ArgumentCaptor<List<News>> captor = ArgumentCaptor.forClass(List.class);
            verify(newsService).addAll(captor.capture());
            assertThat(captor.getValue())
                    .extracting(News::getTitle, News::getLink, News::getKeyword)
                    .containsExactlyInAnyOrder(
                            tuple("코스피 뉴스", "https://news.test/kospi", KeywordType.KOSPI.getName()),
                            tuple("코스닥 뉴스", "https://news.test/kosdaq", KeywordType.KOSDAQ.getName())
                    );
        }

        @Test
        @DisplayName("조회된 뉴스가 없으면 저장을 요청하지 않고 빈 목록을 전달한다")
        void saveEmptyListWhenNoNewsFetched() {
            // given
            when(fetchNewsPort.getNews(any())).thenReturn(List.of());

            // when
            newsManagementService.createPostsFromNews();

            // then
            ArgumentCaptor<List<News>> captor = ArgumentCaptor.forClass(List.class);
            verify(newsService).addAll(captor.capture());
            assertThat(captor.getValue()).isEmpty();
        }
    }

    @Nested
    @DisplayName("오래된 뉴스 삭제")
    class DeleteNews {
        @Test
        @DisplayName("보관기간이 지난 뉴스 삭제를 요청한다")
        void requestDeleteNewsBeforeRetentionPeriod() {
            // given
            LocalDateTime before = LocalDateTime.now().minusDays(1);

            // when
            newsManagementService.deleteNews();

            // then
            LocalDateTime after = LocalDateTime.now().minusDays(1);
            ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
            verify(newsService).deleteByDateTime(captor.capture());
            assertThat(captor.getValue()).isBetween(before, after);
        }
    }
}
