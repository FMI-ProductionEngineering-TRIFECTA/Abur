package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.DeveloperInput;

import java.util.List;
import java.util.Optional;

@Service
public class DeveloperService {

    @Autowired
    UserRepository userRepository;

    public UserEntity getDeveloperById(String id) {
        return userRepository.findByIdAndRole(id, UserEntity.Role.DEVELOPER);
    }

    public List<UserEntity> getAllDevelopers() {
        return userRepository.findByRole(UserEntity.Role.DEVELOPER);
    }

    public UserEntity updateLoggedDeveloper(DeveloperInput developerInput) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String userId) {
            // TODO: refactor -> Optional pentru atributele DeveloperInput
            Optional<UserEntity> developer = userRepository.findById(userId);
            if (developer.isPresent()) {
                developer.get().setUsername(developerInput.getUsername());
                developer.get().setPassword(developerInput.getPassword());
                developer.get().setEmail(developerInput.getEmail());
                developer.get().setDetails(UserEntity.UserDetails.forDeveloper(
                        developerInput.getStudio(),
                        developerInput.getWebsite()
                ));

                return userRepository.save(developer.get());
            }
        }

        return null;
    }

}
