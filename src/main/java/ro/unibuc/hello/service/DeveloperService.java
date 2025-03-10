package ro.unibuc.hello.service;

import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.dto.User;
import ro.unibuc.hello.exception.ValidationException;

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

        // TODO
        // validate("Studio", developer.getStudio(), defaultValidator().and(validateUnique(userRepository::findByDetailsStudio, developer.getStudio())));
        if (userRepository.findByDetailsStudio(developer.getStudio()) != null) {
            throw new ValidationException("The studio `%s` already exists", developer.getStudio());
        }

        validate("Website", developer.getWebsite(), validWebsite());
    }

}
