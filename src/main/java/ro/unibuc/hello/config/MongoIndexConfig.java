package ro.unibuc.hello.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Configuration
public class MongoIndexConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void createIndexes() {
        IndexOperations gamesIndexOps = mongoTemplate.indexOps("games");
        IndexOperations usersIndexOps = mongoTemplate.indexOps("users");

        gamesIndexOps.ensureIndex(new Index().on("title", Sort.Direction.ASC).unique());
        usersIndexOps.ensureIndex(new Index().on("username", Sort.Direction.ASC).unique());
        usersIndexOps.ensureIndex(new Index().on("email", Sort.Direction.ASC).unique());
        // TODO: search if we can do this in mongo
        // usersIndexOps.ensureIndex(new Index().on("details.studio", Sort.Direction.ASC).unique());
    }
}
