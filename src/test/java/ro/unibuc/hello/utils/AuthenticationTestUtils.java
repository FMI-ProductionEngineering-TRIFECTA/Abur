package ro.unibuc.hello.utils;

import org.json.JSONObject;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Credentials;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.dto.User;
import ro.unibuc.hello.security.AuthenticationUtils;
import ro.unibuc.hello.security.jwt.JWTAuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import ro.unibuc.hello.service.AuthenticationService;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ro.unibuc.hello.data.entity.UserEntity.*;

public interface AuthenticationTestUtils {

    UserRepository userRepository = Mockito.mock(UserRepository.class);

    SecurityContext securityContext = Mockito.mock(SecurityContext.class);

    ObjectMapper objectMapper = new ObjectMapper();

    AuthenticationService authService = Mockito.mock(AuthenticationService.class);

    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(authService).build();

    static <B> B buildCommonFields(B builder, Role role) {
        String username = role.toString().toLowerCase();
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

        String username = role.toString().toLowerCase();
        String userId = String.format("%s_id", username);
        JWTAuthenticationToken authToken = new JWTAuthenticationToken(userId);
        when(securityContext.getAuthentication()).thenReturn(authToken);

        UserEntity user = buildCommonFields(UserEntity.builder(), role).games(new ArrayList<>()).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByIdAndRole(userId, role)).thenReturn(user);
        return user;
    }

    static UserEntity mockDeveloperAuth() {
        return mockUserAuth(UserEntity.Role.DEVELOPER);
    }

    @SuppressWarnings("unused")
    static UserEntity mockCustomerAuth() {
        return mockUserAuth(UserEntity.Role.CUSTOMER);
    }

    static String getAccessToken(Role role) throws Exception {
        User user = role == Role.DEVELOPER
                ? buildCommonFields(Developer.builder(), role).studio("DevStudio").build()
                : buildCommonFields(Customer.builder(), role).build();
        String loginBody = objectMapper.writeValueAsString(new Credentials(user.getUsername(), user.getPassword()));
        when(userRepository.findByUsername(user.getUsername())).thenReturn(mockUserAuth(role));

        return new JSONObject(mockMvc
                .perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
        ).getString("token");
    }

    static RequestPostProcessor addToken(String token) {
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }

}
