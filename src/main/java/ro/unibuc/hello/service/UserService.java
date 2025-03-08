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
import ro.unibuc.hello.dto.ErrorString;
import ro.unibuc.hello.dto.User;
import ro.unibuc.hello.security.AuthenticationUtils;

import static ro.unibuc.hello.utils.ResponseUtils.*;
import static ro.unibuc.hello.utils.ValidationUtils.*;

@Service
@SuppressWarnings("unchecked")
public abstract class UserService<T extends User> {

    @Autowired
    private UserRepository userRepository;

    private static ResponseEntity<ErrorString> err;

    protected abstract UserEntity.Role getRole();

    public ResponseEntity<?> getUserById(String id) {
        return ok(userRepository.findByIdAndRole(id, getRole()));
    }

    public ResponseEntity<?> getAllUsers() {
        return ok(userRepository.findByRole(getRole()));
    }

    public ResponseEntity<?> getGames() {
        UserEntity user = AuthenticationUtils.getAuthorizedUser(getRole());
        if (user == null) return unauthorized();

        return ok(user.getDetails().getGames());
    }

    public ResponseEntity<?> getGames(String id) {
        UserEntity user = userRepository.findByIdAndRole(id, getRole());
        return ok(user.getDetails().getGames());
    }

    private ResponseEntity<ErrorString> updateSpecificFields(T userInput, UserEntity user) {
        if (userInput instanceof Customer customerInput) {
            err = chain
            (
                    validateAndUpdate("First name", user.getDetails()::setFirstName, customerInput.getFirstName()),
                    validateAndUpdate("Last name", user.getDetails()::setLastName, customerInput.getLastName())
            );
        }
        else if (userInput instanceof Developer developerInput) {
            err = chain
            (
                    validateAndUpdate("Studio", user.getDetails()::setStudio, developerInput.getStudio()),
                    validateAndUpdate("Website", user.getDetails()::setWebsite, developerInput.getWebsite(), validWebsite())
            );
        }

        return err;
    }

    public ResponseEntity<?> updateLoggedUser(T userInput) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByIdAndRole((String) auth.getPrincipal(), getRole());

        err = chain
        (
                validateAndUpdate("Username", user::setUsername, userInput.getUsername()),
                validateAndUpdate("Password", user::setPassword, userInput.getPassword(), validPassword().and(validLength(5))),
                validateAndUpdate("Email", user::setEmail, userInput.getEmail(), validEmail()),
                updateSpecificFields(userInput, user)
        );

        return err != null ? err : ok(userRepository.save(user));
    }

}
