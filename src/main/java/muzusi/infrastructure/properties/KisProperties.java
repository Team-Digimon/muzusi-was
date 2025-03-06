package muzusi.infrastructure.properties;

import lombok.Getter;
import lombok.Setter;
import muzusi.infrastructure.kis.constant.KisUrlConstant;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    public List<Pair<String, String>> getAuthKeys() {
        List<Pair<String, String>> authKeys = new ArrayList<>();

        for (int idx = 0; idx < Math.min(appKey.size(), appSecret.size()); idx++) {
            authKeys.add(Pair.of(appKey.get(idx), appSecret.get(idx)));
        }

        return authKeys;
    }
}