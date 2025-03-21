package muzusi.infrastructure.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.properties.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@RequiredArgsConstructor
@EnableMongoAuditing
public class MongoConfig extends AbstractMongoClientConfiguration {

    private final MongoProperties mongoProperties;

    @Override
    @Bean
    public MongoClient mongoClient(){
        return MongoClients.create(mongoProperties.getConnectionUri());
    }

    @Override
    protected String getDatabaseName() {
        return mongoProperties.getDatabase();
    }

}
