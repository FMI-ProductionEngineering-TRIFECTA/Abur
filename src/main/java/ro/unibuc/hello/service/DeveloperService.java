package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Developer;

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

    public UserEntity updateLoggedDeveloper(Developer developerInput) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String userId) {
            Optional<UserEntity> developer = userRepository.findById(userId);
            if (developer.isPresent()) {
                if (developerInput.getUsername() != null && !developerInput.getUsername().isBlank()) {
                    developer.get().setUsername(developerInput.getUsername());
                }
                if (developerInput.getPassword() != null && !developerInput.getPassword().isBlank()) {
                    developer.get().setPassword(developerInput.getPassword());
                }
                if (developerInput.getEmail() != null && !developerInput.getEmail().isBlank()) {
                    developer.get().setEmail(developerInput.getEmail());
                }
                if (developerInput.getStudio() != null && !developerInput.getStudio().isBlank()) {
                    developer.get().getDetails().setStudio(developerInput.getStudio());
                }
                if (developerInput.getWebsite() != null && !developerInput.getWebsite().isBlank()) {
                    developer.get().getDetails().setWebsite(developerInput.getWebsite());
                }
                
                return userRepository.save(developer.get());
            }
        }

        return null;
    }

}
