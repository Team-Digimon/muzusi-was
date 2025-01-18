package muzusi.infrastructure.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.webhook.dto.Message;
import muzusi.infrastructure.properties.DiscordWebhookProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordWebhookClient {
    private final DiscordWebhookProperties discordWebhookProperties;

    public void sendWebhookMessage(Message message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Message> requestInfo = new HttpEntity<>(message, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            restTemplate.exchange(
                    discordWebhookProperties.getUrl(),
                    HttpMethod.POST,
                    requestInfo,
                    String.class
            );
        } catch (Exception e) {
            log.error("[DISCORD WEBHOOK ERROR] {}", e.getMessage());
        }
    }
}