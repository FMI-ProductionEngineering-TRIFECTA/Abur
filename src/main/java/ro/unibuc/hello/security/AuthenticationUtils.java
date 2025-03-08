package ro.unibuc.hello.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;

@Component
public class AuthenticationUtils {

    private static UserRepository userRepository;
    private static BCryptPasswordEncoder passwordEncoder;

    @Value("${security.password.encoder.strength}")
    private int encoderStrength;

    @Autowired
    public AuthenticationUtils(UserRepository userRepository) {
        AuthenticationUtils.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        passwordEncoder = new BCryptPasswordEncoder(encoderStrength);
    }

    public static String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public static boolean hasAccess(UserEntity.Role role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String userId) {
            return userRepository.findByIdAndRole(userId, role) != null;
        }
        return false;
    }

    public static boolean isPasswordValid(String providedPassword, String actualPassword) {
        return passwordEncoder.matches(providedPassword, actualPassword);
    }
}