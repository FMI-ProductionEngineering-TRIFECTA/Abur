package ro.unibuc.hello.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.LibraryEntity;
import ro.unibuc.hello.data.entity.UserEntity;

import java.util.List;

/**
 * No need to implement this interface.
 * Spring Data MongoDB automatically creates a class it implementing the interface when you run the application.
 */
@Repository
public interface LibraryRepository extends MongoRepository<LibraryEntity, String> {

    List<GameEntity> findGamesByCustomer(UserEntity customer);

}
