package ro.unibuc.hello.utils;

import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.User;
import ro.unibuc.hello.security.AuthenticationUtils;
import ro.unibuc.hello.security.jwt.JWTAuthenticationToken;
import ro.unibuc.hello.security.jwt.JWTService;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static ro.unibuc.hello.data.entity.UserEntity.*;

public interface AuthenticationTestUtils {

    UserRepository userRepository = Mockito.mock(UserRepository.class);

    SecurityContext securityContext = Mockito.mock(SecurityContext.class);

    JWTService jwtService = new JWTService();

    static <B> B buildCommonFields(B builder, Role role) {
        String username = role.toString().toLowerCase();
        String userId = String.format("%s_id", username);
        String password = String.format("%s123", username);
        String email = String.format("%s@gmail.com", username.toLowerCase());

        if (builder instanceof User.UserBuilder) {
            ((User.UserBuilder<?, ?>) builder)
                    .username(username)
                    .password(password)
                    .email(email);
        }
        else if (builder instanceof UserEntity.UserEntityBuilder) {
            ((UserEntity.UserEntityBuilder) builder)
                    .id(userId)
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role);
        }
        else {
            throw new IllegalArgumentException("Unsupported builder type: " + builder.getClass());
        }
        return builder;
    }

    private static UserEntity mockUserAuth(UserEntity.Role role) {
        AuthenticationUtils.setUserRepository(userRepository);
        SecurityContextHolder.setContext(securityContext);

        UserEntity user = buildCommonFields(UserEntity.builder(), role).games(new ArrayList<>()).build();
        JWTAuthenticationToken authToken = new JWTAuthenticationToken(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByIdAndRole(user.getId(), role)).thenReturn(user);
        when(securityContext.getAuthentication()).thenReturn(authToken);

        return user;
    }

    static UserEntity mockDeveloperAuth() {
        return mockUserAuth(UserEntity.Role.DEVELOPER);
    }

    @SuppressWarnings("unused")
    static UserEntity mockCustomerAuth() {
        return mockUserAuth(UserEntity.Role.CUSTOMER);
    }

    static String getAccessToken(Role role) {
        UserEntity user = buildCommonFields(UserEntity.builder(), role).build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByIdAndRole(user.getId(), role)).thenReturn(user);
        return jwtService.getToken(user.getId());
    }

    static RequestPostProcessor addToken(String token) {
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }

}
