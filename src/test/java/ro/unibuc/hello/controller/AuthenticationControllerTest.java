package ro.unibuc.hello.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Credentials;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.dto.Token;
import ro.unibuc.hello.exception.ValidationException;
import ro.unibuc.hello.service.AuthenticationService;
import ro.unibuc.hello.utils.GenericControllerTest;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.*;

@EnableAspectJAutoProxy
public class AuthenticationControllerTest extends GenericControllerTest<AuthenticationController> {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Override
    public String getEndpoint() {
        return "auth";
    }

    @Override
    public AuthenticationController getController() {
        return authenticationController;
    }

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        super.setUp();
    }

    @Test
    void testLogin_ValidCredentials() throws Exception {
        Developer mockDeveloper = mockDeveloperInput();
        Credentials credentials = new Credentials(mockDeveloper.getUsername(), mockDeveloper.getPassword());
        Token token = new Token(getMockedAccessToken(UserEntity.Role.DEVELOPER));
        when(authenticationService.login(argThat(cred -> cred.equals(credentials))))
                .thenReturn(token);

        performPost(credentials, null, "/login")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token.getToken()));
    }

    @Test
    void testLogin_InvalidUsername() throws Exception {
        Credentials credentials = new Credentials("customer-invalid", "customer123-PASSWORD");

        String errorMessage = "Invalid username or password";
        when(authenticationService.login(argThat(cred -> cred.equals(credentials))))
                .thenThrow(new ValidationException(errorMessage));

        performPost(credentials, null, "/login")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testLogin_NullUsername() throws Exception {
        Credentials credentials = new Credentials(null, "customer123-PASSWORD");

        String errorMessage = "Username is required";
        when(authenticationService.login(credentials)).thenThrow(new ValidationException(errorMessage));

        performPost(credentials, null, "/login")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testLogin_BlankUsername() throws Exception {
        Credentials credentials = new Credentials("   ", "customer123-PASSWORD");

        String errorMessage = "Username cannot be blank";
        when(authenticationService.login(argThat(cred -> cred.equals(credentials))))
                .thenThrow(new ValidationException(errorMessage));

        performPost(credentials, null, "/login")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testLogin_InvalidPassword() throws Exception {
        Credentials credentials = new Credentials("customer", "customer123-invalid");

        String errorMessage = "Invalid username or password";
        when(authenticationService.login(argThat(cred -> cred.equals(credentials))))
                .thenThrow(new ValidationException(errorMessage));

        performPost(credentials, null, "/login")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testLogin_NullPassword() throws Exception {
        Credentials credentials = new Credentials("customer", null);

        String errorMessage = "Password is required";
        when(authenticationService.login(argThat(cred -> cred.equals(credentials))))
                .thenThrow(new ValidationException(errorMessage));

        performPost(credentials, null, "/login")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testLogin_BlankPassword() throws Exception {
        Credentials credentials = new Credentials("customer", "\n\n\n");

        String errorMessage = "Password cannot be blank";
        when(authenticationService.login(argThat(cred -> cred.equals(credentials))))
                .thenThrow(new ValidationException(errorMessage));

        performPost(credentials, null, "/login")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testSignupDeveloper_ValidInput() throws Exception {
        Developer developerInput = mockDeveloperInput();
        UserEntity mockDeveloper = mockDeveloperAuth();
        when(authenticationService.signupDeveloper(argThat(devInput -> devInput.equals(developerInput))))
                .thenReturn(mockDeveloper);

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isCreated())
                .andExpect(matchOne(mockDeveloper, DEVELOPER_FIELDS));
    }

    @Test
    void testSignupCustomer_ValidInput() throws Exception {
        Customer customerInput = mockCustomerInput();
        UserEntity mockCustomer = mockCustomerAuth();
        when(authenticationService.signupCustomer(argThat(custInput -> custInput.equals(customerInput))))
                .thenReturn(mockCustomer);

        performPost(customerInput, null, "/signup/customer")
                .andExpect(status().isCreated())
                .andExpect(matchOne(mockCustomer, CUSTOMER_FIELDS));
    }

}
