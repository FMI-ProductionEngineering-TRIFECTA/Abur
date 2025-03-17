package ro.unibuc.hello.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.exception.NotFoundException;

import java.util.List;

import static ro.unibuc.hello.data.entity.UserEntity.Role;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    UserEntity findByUsername(String username);
    UserEntity findByEmail(String email);
    List<UserEntity> findByRole(Role role);
    UserEntity findByIdAndRole(String id, Role role);
    UserEntity findByDetailsStudio(String studio);

    private UserEntity getUser(String userId, Role role) {
        UserEntity user = findByIdAndRole(userId, role);
        if (user == null) throw new NotFoundException("No %s found at id %s", role.toString().toLowerCase(), userId);
        return user;
    }

    default UserEntity getCustomer(String customerId) {
        return getUser(customerId, Role.CUSTOMER);
    }

    @SuppressWarnings("unused")
    default UserEntity getDeveloper(String developerId) {
        return getUser(developerId, Role.DEVELOPER);
    }

}
