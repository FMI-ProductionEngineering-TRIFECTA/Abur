package ro.unibuc.hello.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.dto.Credentials;
import ro.unibuc.hello.dto.Token;
import ro.unibuc.hello.security.jwt.JWTService;

@Service
public class AuthenticationService {

    @Autowired
    JWTService jwtService;

    @Autowired
    UserRepository userRepository;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public Token login(Credentials credentials) {
        UserEntity user = userRepository.findByUsername(credentials.getUsername());

        if (user != null && isPasswordValid(credentials.getPassword(), user.getPassword())) {
            return new Token(jwtService.getToken(user.getId()));
        }

        return new Token(null);
    }

    public UserEntity signupDeveloper(Developer developer) {
        return userRepository.save(new UserEntity(
                developer.getUsername(),
                developer.getPassword(),
                developer.getEmail(),
                UserEntity.Role.DEVELOPER,
                UserEntity.UserDetails.forDeveloper(
                        developer.getStudio(),
                        developer.getWebsite()
                )
        ));
    }

    public UserEntity signupCustomer(Customer customer) {
        return userRepository.save(new UserEntity(
                customer.getUsername(),
                customer.getPassword(),
                customer.getEmail(),
                UserEntity.Role.CUSTOMER,
                UserEntity.UserDetails.forCustomer(
                        customer.getFirstName(),
                        customer.getLastName()
                )
        ));
    }

    public static String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean hasAccess(UserEntity.Role role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String userId) {
            return userRepository.findByIdAndRole(userId, role) != null;
        }

        return false;
    }

    private static boolean isPasswordValid(String providedPassword, String actualPassword) {
        return passwordEncoder.matches(providedPassword, actualPassword);
    }

}
