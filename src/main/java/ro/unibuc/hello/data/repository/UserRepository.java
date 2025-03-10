package ro.unibuc.hello.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.entity.UserEntity;

import java.util.List;

/**
 * No need to implement this interface.
 * Spring Data MongoDB automatically creates a class it implementing the interface when you run the application.
 */
@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    UserEntity findByUsername(String username);
    UserEntity findByEmail(String email);
    List<UserEntity> findByRole(UserEntity.Role role);
    UserEntity findByIdAndRole(String id, UserEntity.Role role);
    UserEntity findByDetailsStudio(String studio);

}
