package muzusi.application.webhook.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.webhook.dto.Message;
import muzusi.infrastructure.webhook.DiscordWebhookClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscordWebhookService {
    private final DiscordWebhookClient discordWebhookClient;

    public void sendDiscordWebhookMessage(Message message) {
        discordWebhookClient.sendWebhookMessage(message);
    }
}