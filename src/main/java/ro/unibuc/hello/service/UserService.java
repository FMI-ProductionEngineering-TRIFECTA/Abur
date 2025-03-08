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

import java.util.List;

import static ro.unibuc.hello.utils.ValidationUtils.*;

@Service
@SuppressWarnings("unchecked")
public abstract class UserService<T extends User> {

    @Autowired
    private UserRepository userRepository;

    private static ResponseEntity<String> err;

    protected abstract UserEntity.Role getRole();

    public UserEntity getUserById(String id) {
        return userRepository.findByIdAndRole(id, getRole());
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findByRole(getRole());
    }

    private ResponseEntity<String> updateSpecificFields(T userInput, UserEntity user) {
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
        if (auth != null && auth.getPrincipal() instanceof String userId) {
            UserEntity user = userRepository.findByIdAndRole(userId, getRole());

            err = chain
            (
                    validateAndUpdate("Username", user::setUsername, userInput.getUsername()),
                    validateAndUpdate("Password", user::setPassword, userInput.getPassword(), validPassword()),
                    validateAndUpdate("Email", user::setEmail, userInput.getEmail(), validEmail()),
                    updateSpecificFields(userInput, user)
            );

            return err != null ? err : ResponseEntity.ok(userRepository.save(user));
        }

        return null;
    }

}
