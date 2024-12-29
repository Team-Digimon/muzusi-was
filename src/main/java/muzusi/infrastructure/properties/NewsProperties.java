package muzusi.infrastructure.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "news")
@Getter @Setter
public class NewsProperties {
    private String clientId;
    private String clientSecret;
    private String newsApiUrl;
}
