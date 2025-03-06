package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.DeveloperEntity;
import ro.unibuc.hello.data.DeveloperRepository;
import ro.unibuc.hello.dto.DeveloperInput;

import java.util.List;
import java.util.Optional;

@Service
public class DeveloperService {

    @Autowired
    DeveloperRepository developerRepository;

    public DeveloperEntity getDeveloperById(String id) {
        return (developerRepository.findById(id)).get();
    }

    public List<DeveloperEntity> getAllDevelopers() {
        return developerRepository.findAll();
    }

    public DeveloperEntity updateLoggedDeveloper(DeveloperInput developerInput) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String userId) {
            // TODO: refactor -> Optional pentru atributele DeveloperInput
            Optional<DeveloperEntity> developer = developerRepository.findById(userId);
            if (developer.isPresent()) {
                developer.get().setUsername(developerInput.getUsername());
                developer.get().setPassword(developerInput.getPassword());
                developer.get().setEmail(developerInput.getEmail());
                developer.get().setStudio(developerInput.getStudio());
                developer.get().setWebsite(developerInput.getWebsite());

                return developerRepository.save(developer.get());
            }
        }

        return null;
    }

}
