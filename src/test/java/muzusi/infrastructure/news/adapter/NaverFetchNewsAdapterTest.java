package muzusi.infrastructure.news.adapter;

import muzusi.infrastructure.news.client.NaverNewsApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NaverFetchNewsAdapterTest {
    @Mock
    private NaverNewsApiClient naverNewsClient;

    @InjectMocks
    private NaverFetchNewsAdapter naverFetchNewsAdapter;

    @Nested
    @DisplayName("뉴스 조회")
    class GetNews {
        @Test
        @DisplayName("검색어로 네이버 뉴스 API를 호출하고, 조회된 뉴스 목록을 그대로 반환한다")
        void successReturnNewsList() {
            // given
            String query = "코스피";
            List<Map<String, String>> newsItems = List.of(
                    Map.of("title", "제목1", "link", "https://news.test/1", "pubDate", "2026-08-25T00:00:00")
            );
            when(naverNewsClient.fetchNews(query)).thenReturn(newsItems);

            // when
            List<Map<String, String>> result = naverFetchNewsAdapter.getNews(query);

            // then
            assertThat(result).isEqualTo(newsItems);
            verify(naverNewsClient).fetchNews(query);
        }

        @Test
        @DisplayName("조회된 뉴스가 없으면 빈 목록을 반환한다")
        void successReturnEmptyListWhenNoNews() {
            // given
            String query = "코스닥";
            when(naverNewsClient.fetchNews(query)).thenReturn(List.of());

            // when
            List<Map<String, String>> result = naverFetchNewsAdapter.getNews(query);

            // then
            assertThat(result).isEmpty();
            verify(naverNewsClient).fetchNews(query);
        }
    }
}
