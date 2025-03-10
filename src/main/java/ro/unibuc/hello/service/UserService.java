package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.dto.User;

import static ro.unibuc.hello.security.AuthenticationUtils.*;
import static ro.unibuc.hello.utils.ResponseUtils.*;
import static ro.unibuc.hello.utils.ValidationUtils.*;

@Service
public abstract class UserService<T extends User> {

    @Autowired
    protected UserRepository userRepository;

    protected abstract UserEntity.Role getRole();

    protected abstract void validateDetails(User user);

    public ResponseEntity<?> getUserById(String id) {
        return ok(userRepository.findByIdAndRole(id, getRole()));
    }

    public ResponseEntity<?> getAllUsers() {
        return ok(userRepository.findByRole(getRole()));
    }

    public ResponseEntity<?> getGames() {
        UserEntity user = getAuthorizedUser(getRole());
        return ok(user.getGames());
    }

    public ResponseEntity<?> getGames(String id) {
        UserEntity user = userRepository.findByIdAndRole(id, getRole());
        return ok(user.getGames());
    }

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

    public ResponseEntity<?> updateLoggedUser(T userInput, UserEntity user) {
        String username = userInput.getUsername();
        validate(String.format("Username %s", username), username, isUnique(() -> userRepository.findByUsername(username)));
        validateAndUpdate("Username", user::setUsername, username);

        validateAndUpdate("Password", user::setPassword, userInput.getPassword(), validPassword().and(validLength(5)));

        String email = userInput.getEmail();
        validate(String.format("Email %s", email), email, isUnique(() -> userRepository.findByEmail(email)));
        validateAndUpdate("Email", user::setEmail, email, validEmail());

        updateSpecificFields(userInput, user);
        return ok(userRepository.save(user));
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

}
