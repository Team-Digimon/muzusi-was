package muzusi.infrastructure.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
@ConfigurationProperties(prefix = "webhook.discord")
@Getter @Setter
public class DiscordWebhookProperties {
    private String url;
}