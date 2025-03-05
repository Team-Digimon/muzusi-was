package muzusi.infrastructure.config;

import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.kis.websocket.KisWebSocketRouter;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class KisWebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final KisProperties kisProperties;
    private final KisWebSocketRouter kisWebSocketRouter;
    private final WebSocketClient client = new StandardWebSocketClient();

    @Bean
    @Primary
    public WebSocketConnectionManager webSocketConnectionManager1() {
        return new WebSocketConnectionManager(
                client,
                kisWebSocketRouter,
                kisProperties.getWebSocketDomain()
        );
    }

    @Bean
    public WebSocketConnectionManager webSocketConnectionManager2() {
        return new WebSocketConnectionManager(
                client,
                kisWebSocketRouter,
                kisProperties.getWebSocketDomain()
        );
    }
}