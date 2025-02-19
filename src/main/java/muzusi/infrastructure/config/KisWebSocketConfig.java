package muzusi.infrastructure.config;

import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.kis.KisRealTimeTradeHandler;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    private final KisRealTimeTradeHandler kisRealTimeTradeHandler;

    @Bean
    public WebSocketConnectionManager webSocketConnectionManager() {
        WebSocketClient client = new StandardWebSocketClient();
        return new WebSocketConnectionManager(
                client,
                kisRealTimeTradeHandler,
                kisProperties.getWebSocketDomain()
        );
    }
}