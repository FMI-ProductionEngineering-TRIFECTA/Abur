package ro.unibuc.hello.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Collections;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${mongodb.connection.url}")
    private String connectionURL;

    @Override
    @NonNull
    protected String getDatabaseName() {
        return "test";
    }

    @Override
    @NonNull
    public MongoClient mongoClient() {
        MongoClientSettings mongoClientSettings = MongoClientSettings
                .builder()
                .applyConnectionString(new ConnectionString(connectionURL))
                .build();

        return MongoClients.create(mongoClientSettings);
    }

    @Override
    @NonNull
    public Collection<String> getMappingBasePackages() {
        return Collections.singleton("ro.unibuc.hello.data");
    }

}
