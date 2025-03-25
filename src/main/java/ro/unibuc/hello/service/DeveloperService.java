package ro.unibuc.hello.service;

import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.DeveloperOnly;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.dto.User;
import ro.unibuc.hello.security.AuthenticationUtils;

import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.utils.ValidationUtils.*;

@Service
public class DeveloperService extends UserService<Developer> {

    @Override
    protected Role getRole() {
        return Role.DEVELOPER;
    }

    @Override
    protected void validateDetails(User user) {
        Developer developer = (Developer) user;

        String studio = developer.getStudio();
        validate(String.format("Studio %s", studio), studio, isUnique(() -> userRepository.findByDetailsStudio(studio)));

        validate("Website", developer.getWebsite(), validWebsite());
    }

    public UserEntity getDeveloper(String developerId) {
        return getUser(developerId);
    }

    @Override
    protected void updateSpecificFields(Developer userInput, UserEntity user) {
        String studio = userInput.getStudio();
        validate(String.format("Studio %s", studio), studio, isUnique(() -> userRepository.findByDetailsStudio(studio)));
        validateAndUpdate("Studio", user.getDetails()::setStudio, studio);

        validateAndUpdate("Website", user.getDetails()::setWebsite, userInput.getWebsite(), validWebsite());
    }

    @DeveloperOnly
    public UserEntity updateLoggedUser(Developer developerInput) {
        return super.updateLoggedUser(developerInput, AuthenticationUtils.getUser());
    }

}
