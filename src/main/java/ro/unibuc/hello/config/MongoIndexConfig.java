package ro.unibuc.hello.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

@Configuration
public class MongoIndexConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    private void uniqueIndexes(MongoCollection<Document> collection, List<String> fields) {
        fields.forEach(field -> collection.createIndex(Indexes.ascending(field), new IndexOptions().unique(true)));
    }

    private void partialIndexes(MongoCollection<Document> collection, List<String> fields) {
        fields.forEach(field -> collection.createIndex(Indexes.ascending(field), new IndexOptions().partialFilterExpression(Filters.exists(field, true))));
    }

    @PostConstruct
    public void createIndexes() {
        MongoCollection<Document> games = mongoTemplate.getCollection("games");
        MongoCollection<Document> users = mongoTemplate.getCollection("users");

        uniqueIndexes(games, List.of("title"));
        uniqueIndexes(users, List.of("username", "email"));

        partialIndexes(users, List.of("details.studio"));
    }

}
