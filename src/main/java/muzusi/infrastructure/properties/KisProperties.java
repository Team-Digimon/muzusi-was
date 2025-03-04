package muzusi.infrastructure.properties;

import lombok.Getter;
import lombok.Setter;
import muzusi.infrastructure.kis.constant.KisUrlConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "kis")
@Getter @Setter
public class KisProperties {
    private String domain;
    private String webSocketDomain;
    private List<String> appKey;
    private List<String> appSecret;

    public String getUrl(KisUrlConstant kisUrlConstant) {
        return domain + kisUrlConstant.getUrl();
    }
}