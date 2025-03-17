package ro.unibuc.hello.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.entity.UserEntity;

import java.util.List;

import static ro.unibuc.hello.data.entity.UserEntity.Role;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    UserEntity findByUsername(String username);
    UserEntity findByEmail(String email);
    List<UserEntity> findByRole(Role role);
    UserEntity findByIdAndRole(String id, Role role);
    UserEntity findByDetailsStudio(String studio);

}
