package ro.unibuc.hello.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.entity.DeveloperEntity;

/**
 * No need to implement this interface.
 * Spring Data MongoDB automatically creates a class it implementing the interface when you run the application.
 */
@Repository
public interface DeveloperRepository extends MongoRepository<DeveloperEntity, String> {

    DeveloperEntity findByUsername(String username);

}
