package muzusi.infrastructure.news.adapter;

import lombok.RequiredArgsConstructor;
import muzusi.application.news.port.FetchNewsPort;
import muzusi.infrastructure.news.client.NaverNewsApiClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NaverFetchNewsAdapter implements FetchNewsPort {
    private final NaverNewsApiClient naverNewsClient;
    
    /**
     * 네이버 뉴스 검색 API를 통해 주어진 검색어의 검색 결과 뉴스 목록을 반환하는 메서드
     *
     * @param query     검색어
     * @return          뉴스 목록
     */
    @Override
    public List<Map<String, String>> getNews(String query) {
        return naverNewsClient.fetchNews(query);
    }
}
