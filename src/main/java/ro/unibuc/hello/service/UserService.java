package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.dto.User;
import ro.unibuc.hello.exception.ValidationException;
import ro.unibuc.hello.security.AuthenticationUtils;

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
        UserEntity user = AuthenticationUtils.getAuthorizedUser(getRole());
        if (user == null) return unauthorized();

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
            // TODO: check unique
            validateAndUpdate("Studio", user.getDetails()::setStudio, developerInput.getStudio());
            validateAndUpdate("Website", user.getDetails()::setWebsite, developerInput.getWebsite(), validWebsite());
        }
    }

    public ResponseEntity<?> updateLoggedUser(T userInput) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByIdAndRole((String) auth.getPrincipal(), getRole());

        // TODO: check unique
        validateAndUpdate("Username", user::setUsername, userInput.getUsername());
        // TODO: check unique
        validateAndUpdate("Password", user::setPassword, userInput.getPassword(), validPassword().and(validLength(5)));
        // TODO: check unique
        validateAndUpdate("Email", user::setEmail, userInput.getEmail(), validEmail());
        updateSpecificFields(userInput, user);

        return ok(userRepository.save(user));
    }

    public void validateUser(User user) {
        validate("Username", user.getUsername(), validLength(5));
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new ValidationException("The username `%s` already exists", user.getUsername());
        }

        validate("Password", user.getUsername(), validPassword().and(validLength(5)));

        validate("Email", user.getEmail());
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new ValidationException("The email `%s` already exists", user.getEmail());
        }

        validateDetails(user);
    }

}
