package muzusi.infrastructure.news.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.news.exception.NaverNewsApiException;
import muzusi.infrastructure.properties.NaverApiHubProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class NaverNewsApiClient {
    private final NaverApiHubProperties naverApiHubProperties;

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    public List<Map<String, String>> fetchNews(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-NCP-APIGW-API-KEY-ID", naverApiHubProperties.getClientId());
        headers.add("X-NCP-APIGW-API-KEY", naverApiHubProperties.getClientSecret());

        URI uri = UriComponentsBuilder.fromHttpUrl(naverApiHubProperties.getSearchNewsUrl())
                .queryParam("query", query)
                .queryParam("display", "10")
                .queryParam("start", "1")
                .queryParam("sort", "date")
                .encode()
                .build()
                .toUri();

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            NaverNewsResponse response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestEntity,
                    NaverNewsResponse.class
            ).getBody();
            
            if (response == null || response.items() == null) {
                return Collections.emptyList();
            }
            
            return response.items().stream()
                    .map(item -> Map.of(
                            "title", stripHtml(item.title()),
                            "link", item.link(),
                            "pubDate", item.pubDate()
                    ))
                    .toList();
        } catch (Exception e) {
            throw new NaverNewsApiException("네이버 뉴스 조회 API 호출 중 에러가 발생하였습니다.", e);
        }
    }
    
    /**
     * 문자열 포함된 html 태그를 제거하는 메서드
     *
     * <p> 네이버 뉴스 제목에 포함된 html 태그를 제거하기 위해 사용한다.
     *
     * @param input 입력 문자열
     * @return      html 태그가 제거된 문자열
     */
    private String stripHtml(String input) {
        if (input == null) {
            return null;
        }
        return HTML_TAG_PATTERN.matcher(input).replaceAll("");
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    record NaverNewsResponse(List<Item> items) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Item(
                @JsonProperty(value = "title") String title,
                @JsonProperty(value = "originallink") String originalLink,
                @JsonProperty(value = "link") String link,
                @JsonProperty(value = "description") String description,
                @JsonProperty(value = "pubDate") String pubDate
        ) { }
    }
}