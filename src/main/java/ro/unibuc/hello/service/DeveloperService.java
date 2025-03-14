package ro.unibuc.hello.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.annotation.DeveloperOnly;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.dto.User;
import ro.unibuc.hello.security.UserContext;

import java.util.Objects;

import static ro.unibuc.hello.utils.ValidationUtils.*;

@Service
public class DeveloperService extends UserService<Developer> {

    @Override
    protected UserEntity.Role getRole() {
        return UserEntity.Role.DEVELOPER;
    }

    @Override
    protected void validateDetails(User user) {
        Developer developer = (Developer) user;
        String studio = developer.getStudio();

        validate("Studio", studio, defaultValidator().and(isUnique(() -> userRepository.findByDetailsStudio(studio))));
        validate("Website", developer.getWebsite(), validWebsite());
    }

    @DeveloperOnly
    public ResponseEntity<?> updateLoggedUser(Developer developerInput) {
        return super.updateLoggedUser(developerInput, UserContext.getUser());
    }

}
