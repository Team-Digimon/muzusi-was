package muzusi.infrastructure.news;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import muzusi.global.exception.CustomException;
import muzusi.global.exception.NewsApiException;
import muzusi.global.response.error.type.CommonErrorType;
import muzusi.infrastructure.properties.NewsProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class NewsApiClient {
    private final NewsProperties newsProperties;
    private final ObjectMapper objectMapper;

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    public List<Map<String, String>> fetchNews(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", newsProperties.getClientId());
        headers.add("X-Naver-Client-Secret", newsProperties.getClientSecret());

        String uri = UriComponentsBuilder.fromHttpUrl(newsProperties.getNewsApiUrl())
                .queryParam("query", query)
                .queryParam("display", "10")
                .queryParam("start", "1")
                .queryParam("sort", "date")
                .build()
                .toUriString();

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
            List<Map<String, Object>> items = (List<Map<String, Object>>) responseMap.get("items");

            return items.stream()
                    .map(item -> Map.of(
                            "title", stripHtml((String) item.get("title")),
                            "link", (String) item.get("link"),
                            "pubDate", (String) item.get("pubDate")
                    ))
                    .toList();
        } catch (Exception e) {
            throw new NewsApiException(e);
        }
    }

    private String stripHtml(String input) {
        if (input == null) {
            return null;
        }
        return HTML_TAG_PATTERN.matcher(input).replaceAll("");
    }
}

