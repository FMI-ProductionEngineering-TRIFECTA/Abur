package ro.unibuc.hello.utils;

import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.security.AuthenticationUtils;
import ro.unibuc.hello.security.jwt.JWTAuthenticationToken;

import java.util.Optional;

import static org.mockito.Mockito.when;

public interface AuthenticationTestUtils {

    UserRepository userRepository = Mockito.mock(UserRepository.class);

    SecurityContext securityContext = Mockito.mock(SecurityContext.class);

    private static UserEntity mockUserAuth(String username, UserEntity.Role role) {
        AuthenticationUtils.setUserRepository(userRepository);
        SecurityContextHolder.setContext(securityContext);

        String userId = String.format("%s_id", username);
        JWTAuthenticationToken authToken = new JWTAuthenticationToken(userId);
        when(securityContext.getAuthentication()).thenReturn(authToken);

        UserEntity user = new UserEntity(
                username,
                String.format("%s123", username),
                String.format("%s@gmail.com", username.toLowerCase()),
                role,
                null
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByIdAndRole(userId, role)).thenReturn(user);
        return user;
    }

    static UserEntity mockDeveloperAuth() {
        return mockUserAuth("developer", UserEntity.Role.DEVELOPER);
    }

    @SuppressWarnings("unused")
    static UserEntity mockCustomerAuth() {
        return mockUserAuth("customer", UserEntity.Role.CUSTOMER);
    }

}
