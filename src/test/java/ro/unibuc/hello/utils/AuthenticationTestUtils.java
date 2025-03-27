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
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.data.entity.UserEntity.UserDetails;

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

    public static UserEntity mockUpdatedDeveloperAuth() {
        Developer developerInput = mockUpdatedDeveloperInput();
        UserEntity updatedDeveloper = mockUserAuth(UserEntity.Role.DEVELOPER);

        updatedDeveloper.setUsername(developerInput.getUsername());
        updatedDeveloper.setPassword(developerInput.getPassword());
        updatedDeveloper.setEmail(developerInput.getEmail());
        updatedDeveloper.setDetails(UserDetails.forDeveloper(
                developerInput.getStudio(),
                developerInput.getWebsite()
        ));

        return updatedDeveloper;
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

    public static UserEntity mockUpdatedCustomerAuth() {
        Customer customerInput = mockUpdatedCustomerInput();
        UserEntity updatedCustomer = mockUserAuth(UserEntity.Role.CUSTOMER);

        updatedCustomer.setUsername(customerInput.getUsername());
        updatedCustomer.setPassword(customerInput.getPassword());
        updatedCustomer.setEmail(customerInput.getEmail());
        updatedCustomer.setDetails(UserDetails.forCustomer(
                customerInput.getFirstName(),
                customerInput.getLastName()
        ));

        return updatedCustomer;
    }

    public static Developer mockDeveloperInput() {
        return buildCommonFields(Developer.builder(), Role.DEVELOPER)
                .studio("developer-studio")
                .website("https://developer-website.com")
                .build();
    }

    public static Developer mockUpdatedDeveloperInput() {
        Developer updatedDeveloperInput = mockDeveloperInput();
        updatedDeveloperInput.setUsername(updatedDeveloperInput.getUsername() + "-update");
        updatedDeveloperInput.setPassword(updatedDeveloperInput.getPassword() + "-update");
        updatedDeveloperInput.setEmail(updatedDeveloperInput.getUsername() + "@gmail.com");
        updatedDeveloperInput.setStudio(updatedDeveloperInput.getStudio() + "-update");
        updatedDeveloperInput.setWebsite("https://developer-update-website.com");

        return updatedDeveloperInput;
    }

    public static Customer mockCustomerInput() {
        return buildCommonFields(Customer.builder(), Role.CUSTOMER)
                .firstName("customer-firstName")
                .lastName("customer-lastName")
                .build();
    }

    public static Customer mockUpdatedCustomerInput() {
        Customer updatedCustomerInput = mockCustomerInput();
        updatedCustomerInput.setUsername(updatedCustomerInput.getUsername() + "-update");
        updatedCustomerInput.setPassword(updatedCustomerInput.getPassword() + "-update");
        updatedCustomerInput.setEmail(updatedCustomerInput.getUsername() + "@gmail.com");
        updatedCustomerInput.setFirstName(updatedCustomerInput.getFirstName() + "-update");
        updatedCustomerInput.setLastName(updatedCustomerInput.getLastName() + "-update");

        return updatedCustomerInput;
    }

    public static List<UserEntity> buildCustomers(Integer total) {
        List<UserEntity> customers = new ArrayList<>();
        for (int id = 1; id <= total; ++id) {
            customers.add(UserEntity.builder()
                    .id(String.valueOf(id))
                    .username("customer-" + id)
                    .password("customer-PASSWORD-" + id)
                    .email("customer" + id + "@gmail.com")
                    .details(UserDetails.forCustomer(
                            "customer-firstName-" + id,
                            "customer-lastName-" + id + ".com"
                    ))
                    .build()
            );
        }

        return customers;
    }

    public static List<UserEntity> buildDevelopers(Integer total) {
        List<UserEntity> developers = new ArrayList<>();
        for (int id = 1; id <= total; ++id) {
            developers.add(UserEntity.builder()
                    .id(String.valueOf(id))
                    .username("developer-" + id)
                    .password("developer-PASSWORD-" + id)
                    .email("developer" + id + "@gmail.com")
                    .details(UserDetails.forDeveloper(
                            "developer-studio-" + id,
                            "https://developer-website-" + id + ".com"
                    ))
                    .build()
            );
        }

        return developers;
    }

    public static String getMockedAccessToken(Role role) {
        UserEntity user = mockUserAuth(role);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByIdAndRole(user.getId(), role)).thenReturn(user);
        return jwtService.getToken(user.getId());
    }

    public static void resetMockedAccessToken() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        when(userRepository.findByIdAndRole(any(), any())).thenReturn(null);
    }

    public static RequestPostProcessor addToken(String token) {
        return request -> {
            if (token != null) {
                request.addHeader("Authorization", "Bearer " + token);
            }
            return request;
        };
    }

}
