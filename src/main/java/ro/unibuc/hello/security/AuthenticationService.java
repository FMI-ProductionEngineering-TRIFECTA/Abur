package ro.unibuc.hello.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.CustomerInput;
import ro.unibuc.hello.dto.DeveloperInput;
import ro.unibuc.hello.dto.LoginInput;
import ro.unibuc.hello.dto.LoginResult;
import ro.unibuc.hello.security.jwt.JWTService;

@Service
public class AuthenticationService {

    @Autowired
    JWTService jwtService;

    @Autowired
    UserRepository userRepository;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public LoginResult login(LoginInput loginInput) {
        UserEntity user = userRepository.findByUsername(loginInput.getUsername());

        if (user != null && isPasswordValid(loginInput.getPassword(), user.getPassword())) {
            return new LoginResult(jwtService.getToken(user.getId()));
        }

        return new LoginResult(null);
    }

    public UserEntity signupDeveloper(DeveloperInput developerInput) {
        return userRepository.save(new UserEntity(
                developerInput.getUsername(),
                developerInput.getPassword(),
                developerInput.getEmail(),
                UserEntity.Role.DEVELOPER,
                UserEntity.UserDetails.forDeveloper(
                        developerInput.getStudio(),
                        developerInput.getWebsite()
                )
        ));
    }

    public UserEntity signupCustomer(CustomerInput customerInput) {
        return userRepository.save(new UserEntity(
                customerInput.getUsername(),
                customerInput.getPassword(),
                customerInput.getEmail(),
                UserEntity.Role.CUSTOMER,
                UserEntity.UserDetails.forCustomer(
                        customerInput.getFirstName(),
                        customerInput.getLastName()
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
