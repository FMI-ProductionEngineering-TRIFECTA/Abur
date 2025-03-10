package ro.unibuc.hello.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.exception.UnauthorizedAccessException;

@Component
public class AuthenticationUtils {

    private static UserRepository userRepository;
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Autowired
    public AuthenticationUtils(UserRepository userRepository) {
        AuthenticationUtils.userRepository = userRepository;
    }

    public static String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public static UserEntity getAuthorizedUser(UserEntity.Role role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String userId) {
            UserEntity user = userRepository.findByIdAndRole(userId, role);
            if (user != null) return user;
        }
        throw new UnauthorizedAccessException();
    }

    public static boolean isPasswordValid(String providedPassword, String actualPassword) {
        return passwordEncoder.matches(providedPassword, actualPassword);
    }
}