package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.dto.User;
import static ro.unibuc.hello.utils.ValidationUtils.*;

import java.util.List;

@Service
public abstract class UserService<T extends User> {

    @Autowired
    private UserRepository userRepository;

    protected abstract UserEntity.Role getRole();

    public UserEntity getUserById(String id) {
        return userRepository.findByIdAndRole(id, getRole());
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findByRole(getRole());
    }

    private void updateSpecificFields(T userInput, UserEntity user) {
        if (userInput instanceof Customer customerInput) {
            if (isValid(customerInput.getFirstName())) {
                user.getDetails().setFirstName(customerInput.getFirstName());
            }
            if (isValid(customerInput.getLastName())) {
                user.getDetails().setLastName(customerInput.getLastName());
            }
        }
        else if (userInput instanceof Developer developerInput) {
            if (isValid(developerInput.getStudio())) {
                user.getDetails().setStudio(developerInput.getStudio());
            }
            if (isValid(developerInput.getWebsite(), validWebsite())) {
                user.getDetails().setWebsite(developerInput.getWebsite());
            }
        }
    }

    public UserEntity updateLoggedUser(T userInput) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String userId) {
            UserEntity user = userRepository.findByIdAndRole(userId, getRole());
            if (isValid(userInput.getUsername(), validLength(5))) {
                user.setUsername(userInput.getUsername());
            }
            if (isValid(userInput.getPassword(), validLength(5).and(validPassword()))) {
                user.setPassword(userInput.getPassword());
            }
            if (isValid(userInput.getEmail(), validEmail())) {
                user.setEmail(userInput.getEmail());
            }

            updateSpecificFields(userInput, user);
            return userRepository.save(user);
        }

        return null;
    }

}
