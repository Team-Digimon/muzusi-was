package muzusi.infrastructure.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@ConfigurationProperties(prefix = "spring.data.mongodb")
@Setter
public class MongoProperties {
    private String host;
    private String port;
    private String username;
    private String password;
    private String authenticationDatabase;
    @Getter private String database;

    public String getConnectionUri(){
        return UriComponentsBuilder
                .fromUriString("mongodb://{username}:{password}@{host}:{port}/{database}?authSource={authenticationDatabase}")
                .buildAndExpand(username, password, host, port, database, authenticationDatabase)
                .toUriString();
    }

}
