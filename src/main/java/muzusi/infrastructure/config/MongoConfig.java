package muzusi.infrastructure.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.properties.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
public class MongoConfig extends AbstractMongoClientConfiguration {

    private final MongoProperties mongoProperties;

    @Override
    @Bean
    public MongoClient mongoClient(){
        return MongoClients.create(mongoProperties.getConnectionUri());
    }

    @Bean
    public MongoTransactionManager transactionManager(){
        return new MongoTransactionManager(mongoDbFactory());
    }

    @Override
    protected String getDatabaseName() {
        return mongoProperties.getDatabase();
    }

}
