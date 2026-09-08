package muzusi.infrastructure.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter @Setter
@ConfigurationProperties(prefix = "naver.api-hub")
public class NaverApiHubProperties {
    private String clientId;
    private String clientSecret;
    private String baseUrl;
    
    public static final String SEARCH_NEWS_PATH = "/search/v1/news";
    
    public String getSearchNewsUrl() {
        return baseUrl + SEARCH_NEWS_PATH;
    }
}
