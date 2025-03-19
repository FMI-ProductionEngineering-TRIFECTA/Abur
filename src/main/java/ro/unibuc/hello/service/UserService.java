package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.dto.User;
import ro.unibuc.hello.exception.NotFoundException;

import java.util.List;

import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.security.AuthenticationUtils.getAuthorizedUser;
import static ro.unibuc.hello.utils.ValidationUtils.*;

@Service
public abstract class UserService<T extends User> {

    @Autowired
    protected UserRepository userRepository;

    protected abstract Role getRole();

    protected UserEntity getUser(String userId) {
        UserEntity user = userRepository.findByIdAndRole(userId, getRole());
        if (user == null) throw new NotFoundException("No %s found at id %s", getRole().toString().toLowerCase(), userId);
        return user;
    }

    protected abstract void validateDetails(User user);

    private void updateSpecificFields(T userInput, UserEntity user) {
        if (userInput instanceof Customer customerInput) {
            validateAndUpdate("First name", user.getDetails()::setFirstName, customerInput.getFirstName());
            validateAndUpdate("Last name", user.getDetails()::setLastName, customerInput.getLastName());
        }
        else if (userInput instanceof Developer developerInput) {
            String studio = developerInput.getStudio();
            validate(String.format("Studio %s", studio), studio, isUnique(() -> userRepository.findByDetailsStudio(studio)));
            validateAndUpdate("Studio", user.getDetails()::setStudio, studio);

            validateAndUpdate("Website", user.getDetails()::setWebsite, developerInput.getWebsite(), validWebsite());
        }
    }

    public void validateUser(User user) {
        String username = user.getUsername();
        validate(String.format("Username %s", username), username, isUnique(() -> userRepository.findByUsername(username)));
        validate("Username", username, validLength(5));

        validate("Password", user.getPassword(), validPassword().and(validLength(5)));

        String email = user.getEmail();
        validate(String.format("Email %s", email), email, isUnique(() -> userRepository.findByEmail(email)));
        validate("Email", email, validEmail());

        validateDetails(user);
    }

    public UserEntity getUserById(String id) {
        return userRepository.findByIdAndRole(id, getRole());
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findByRole(getRole());
    }

    public List<GameEntity> getGames() {
        UserEntity user = getAuthorizedUser(getRole());
        return user.getGames();
    }

    public List<GameEntity> getGames(String id) {
        UserEntity user = userRepository.findByIdAndRole(id, getRole());
        return user.getGames();
    }

    public UserEntity updateLoggedUser(T userInput, UserEntity user) {
        String username = userInput.getUsername();
        validate(String.format("Username %s", username), username, isUnique(() -> userRepository.findByUsername(username)));
        validateAndUpdate("Username", user::setUsername, username);

        validateAndUpdate("Password", user::setPassword, userInput.getPassword(), validPassword().and(validLength(5)));

        String email = userInput.getEmail();
        validate(String.format("Email %s", email), email, isUnique(() -> userRepository.findByEmail(email)));
        validateAndUpdate("Email", user::setEmail, email, validEmail());

        updateSpecificFields(userInput, user);
        return userRepository.save(user);
    }

}
