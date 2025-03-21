package ro.unibuc.hello.utils;

import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.dto.User;
import ro.unibuc.hello.security.AuthenticationUtils;
import ro.unibuc.hello.security.jwt.JWTAuthenticationToken;
import ro.unibuc.hello.security.jwt.JWTService;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static ro.unibuc.hello.data.entity.UserEntity.*;

public final class AuthenticationTestUtils {

    private AuthenticationTestUtils() {}

    private static final UserRepository userRepository = Mockito.mock(UserRepository.class);

    private static final SecurityContext securityContext = Mockito.mock(SecurityContext.class);

    public static final JWTService jwtService = new JWTService("8a05a7ec81fd773513f88bb33b0ea42b436902d88fc4d9b0dec15d402dfad4c3");

    public static <B> B buildCommonFields(B builder, Role role) {
        String username = role.toString().toLowerCase();
        String userId = String.format("%s_id", username);
        String password = String.format("%s123-PASSWORD", username);
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

    public static UserEntity mockDeveloperAuth() {
        UserEntity mockDeveloper = mockUserAuth(UserEntity.Role.DEVELOPER);
        mockDeveloper.setDetails(UserDetails.forDeveloper(
                "developer-studio",
                "https://developer-website.com"
        ));

        return mockDeveloper;
    }

    @SuppressWarnings("unused")
    public static UserEntity mockCustomerAuth() {
        UserEntity mockCustomer = mockUserAuth(UserEntity.Role.CUSTOMER);
        mockCustomer.setDetails(UserDetails.forCustomer(
                "customer-firstName",
                "customer-lastName"
        ));

        return mockCustomer;
    }

    public static Developer mockDeveloperInput() {
        return buildCommonFields(Developer.builder(), Role.DEVELOPER)
                .studio("developer-studio")
                .website("https://developer-website.com")
                .build();
    }

    public static Customer mockCustomerInput() {
        return buildCommonFields(Customer.builder(), Role.CUSTOMER)
                .firstName("customer-firstName")
                .lastName("customer-lastName")
                .build();
    }

    public static String getAccessToken(Role role) {
        UserEntity user = mockUserAuth(role);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByIdAndRole(user.getId(), role)).thenReturn(user);
        return jwtService.getToken(user.getId());
    }

    public static void resetAccessToken() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        when(userRepository.findByIdAndRole(any(), any())).thenReturn(null);
    }

    public static RequestPostProcessor addToken(String token) {
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }

}
