package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.util.ReflectionTestUtils;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Credentials;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.dto.Token;
import ro.unibuc.hello.exception.ValidationException;
import ro.unibuc.hello.security.AuthenticationUtils;
import ro.unibuc.hello.security.jwt.JWTService;
import ro.unibuc.hello.utils.AuthenticationTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.*;

public class AuthenticationServiceTest {

    private static final Integer MIN_STRING_VALID_LENGTH = 5;

    @Mock
    protected UserRepository userRepository;

    @Spy
    private JWTService jwtService = AuthenticationTestUtils.jwtService;

    @Spy
    protected DeveloperService developerService;

    @Spy
    protected CustomerService customerService;

    @InjectMocks
    private AuthenticationService authenticationService = new AuthenticationService();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resetMockedAccessToken();

        ReflectionTestUtils.setField(developerService, "userRepository", userRepository);
        ReflectionTestUtils.setField(customerService, "userRepository", userRepository);
    }

    @Test
    void testLogin_ValidCredentials() {
        Credentials credentials = new Credentials("customer", "customer123-PASSWORD");
        UserEntity user = mockCustomerAuth();
        when(userRepository.findByUsername(credentials.getUsername())).thenReturn(user);

        Token token = authenticationService.login(credentials);

        assertEquals("eyJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOiJjdXN0b21lcl9pZCJ9.7-uu9F8Odhj9bNGoYmvLfYLh4U-S_be5OfPROggDyOV3QhlAz6nSa3a5rYpyUOty", token.getToken());
        /*  EXPECTED DECODED PAYLOAD
                {
                  "alg": "HS384"
                }
                {
                  "userId": "customer_id"
                }
                HMACSHA384(
                  base64UrlEncode(header) + "." +
                  base64UrlEncode(payload),
                  secret: "8a05a7ec81fd773513f88bb33b0ea42b436902d88fc4d9b0dec15d402dfad4c3"
                ) secret IS base64 encoded
         */

        verify(userRepository, times(1)).findByUsername(argThat(username -> username.equals(credentials.getUsername())));
    }

    @Test
    void testLogin_InvalidUsername() {
        Credentials credentials = new Credentials("customer-invalid", "customer123-PASSWORD");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.login(credentials));
        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepository, times(1)).findByUsername(argThat(username -> username.equals(credentials.getUsername())));
    }

    @Test
    void testLogin_NullUsername() {
        Credentials credentials = new Credentials(null, "customer123-PASSWORD");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.login(credentials));
        assertEquals("Username is required", exception.getMessage());

        verify(userRepository, times(0)).findByUsername(any());
    }

    @Test
    void testLogin_BlankUsername() {
        Credentials credentials = new Credentials("   ", "customer123-PASSWORD");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.login(credentials));
        assertEquals("Username cannot be blank", exception.getMessage());

        verify(userRepository, times(0)).findByUsername(any());
    }

    @Test
    void testLogin_InvalidPassword() {
        Credentials credentials = new Credentials("customer", "customer123-invalid");
        UserEntity user = mockCustomerAuth();
        when(userRepository.findByUsername(credentials.getUsername())).thenReturn(user);

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.login(credentials));
        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepository, times(1)).findByUsername(argThat(username -> username.equals(credentials.getUsername())));
    }

    @Test
    void testLogin_NullPassword() {
        Credentials credentials = new Credentials("customer", null);

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.login(credentials));
        assertEquals("Password is required", exception.getMessage());

        verify(userRepository, times(0)).findByUsername(any());
    }

    @Test
    void testLogin_BlankPassword() {
        Credentials credentials = new Credentials("customer", "\n\n\n");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.login(credentials));
        assertEquals("Password cannot be blank", exception.getMessage());

        verify(userRepository, times(0)).findByUsername(any());
    }

    @Test
    void testSignupDeveloper_ValidInput() {
        Developer developerInput = mockDeveloperInput();
        UserEntity mockDeveloper = mockDeveloperAuth();
        when(userRepository.save(any(UserEntity.class))).thenReturn(mockDeveloper);

        UserEntity developer = authenticationService.signupDeveloper(developerInput);

        assertEquals("developer_id", developer.getId());
        assertEquals(developerInput.getUsername(), developer.getUsername());
        assertTrue(AuthenticationUtils.isPasswordValid(developerInput.getPassword(), developer.getPassword()));
        assertEquals(developerInput.getEmail(), developer.getEmail());
        assertEquals(developerInput.getStudio(), developer.getDetails().getStudio());
        assertEquals(developerInput.getWebsite(), developer.getDetails().getWebsite());
        assertNull(developer.getDetails().getFirstName());
        assertNull(developer.getDetails().getLastName());

        verify(userRepository, times(1)).save(
                argThat(dev
                        -> dev.getUsername().equals(developerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(developerInput.getPassword(), dev.getPassword())
                        && dev.getEmail().equals(developerInput.getEmail())
                        && dev.getRole().equals(UserEntity.Role.DEVELOPER)
                        && dev.getDetails().getStudio().equals(developerInput.getStudio())
                        && dev.getDetails().getWebsite().equals(developerInput.getWebsite())
                        && dev.getDetails().getFirstName() == null
                        && dev.getDetails().getLastName() == null
                )
        );
    }

    @Test
    void testSignupDeveloper_NullUsername() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setUsername(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Username is required", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_BlankUsername() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setUsername("           ");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Username cannot be blank", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_NonUniqueUsername() {
        Developer developerInput = mockDeveloperInput();
        UserEntity developer = mockDeveloperAuth();
        when(userRepository.findByUsername(developerInput.getUsername())).thenReturn(developer);

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals(String.format("Username %s already exists!", developerInput.getUsername()), exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_NoMinLengthUsername() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setUsername("dev");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Username must be at least " + MIN_STRING_VALID_LENGTH + " characters long!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_NullPassword() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setPassword(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Password is required", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_BlankPassword() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setPassword("");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Password cannot be blank", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_NoUppercaseLetterPassword() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setPassword("pass");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Password must contain at least one uppercase letter!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_NoLowercaseLetterPassword() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setPassword("PASS");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Password must contain at least one lowercase letter!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_NoDigitPassword() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setPassword("Pass");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Password must contain at least one digit!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_NoMinLengthPassword() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setPassword("Pa1");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Password must be at least " + MIN_STRING_VALID_LENGTH + " characters long!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_NullEmail() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setEmail(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Email is required", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_BlankEmail() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setEmail(" \n\t");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Email cannot be blank", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_NonUniqueEmail() {
        Developer developerInput = mockDeveloperInput();
        UserEntity developer = mockDeveloperAuth();
        when(userRepository.findByEmail(developerInput.getEmail())).thenReturn(developer);

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals(String.format("Email %s already exists!", developerInput.getEmail()), exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_NoFormatEmail() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setEmail("mail");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Email must be a valid email address!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_NullStudio() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setStudio(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Studio is required", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_BlankStudio() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setStudio("\n");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Studio cannot be blank", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_NonUniqueStudio() {
        Developer developerInput = mockDeveloperInput();
        UserEntity developer = mockDeveloperAuth();
        when(userRepository.findByDetailsStudio(developerInput.getStudio())).thenReturn(developer);

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals(String.format("Studio %s already exists!", developerInput.getStudio()), exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupDeveloper_NoFormatWebsite() {
        Developer developerInput = mockDeveloperInput();
        developerInput.setWebsite("developer-website");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupDeveloper(developerInput));
        assertEquals("Website must be a valid website URL!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupCustomer_ValidInput() {
        Customer customerInput = mockCustomerInput();
        UserEntity mockCustomer = mockCustomerAuth();
        when(userRepository.save(any(UserEntity.class))).thenReturn(mockCustomer);

        UserEntity customer = authenticationService.signupCustomer(customerInput);

        assertEquals("customer_id", customer.getId());
        assertEquals(customerInput.getUsername(), customer.getUsername());
        assertTrue(AuthenticationUtils.isPasswordValid(customerInput.getPassword(), customer.getPassword()));
        assertEquals(customerInput.getEmail(), customer.getEmail());
        assertEquals(customerInput.getFirstName(), customer.getDetails().getFirstName());
        assertEquals(customerInput.getLastName(), customer.getDetails().getLastName());
        assertNull(customer.getDetails().getStudio());
        assertNull(customer.getDetails().getWebsite());

        verify(userRepository, times(1)).save(
                argThat(cus
                        -> cus.getUsername().equals(customerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(customerInput.getPassword(), cus.getPassword())
                        && cus.getEmail().equals(customerInput.getEmail())
                        && cus.getRole().equals(UserEntity.Role.CUSTOMER)
                        && cus.getDetails().getFirstName().equals(customerInput.getFirstName())
                        && cus.getDetails().getLastName().equals(customerInput.getLastName())
                        && cus.getDetails().getStudio() == null
                        && cus.getDetails().getWebsite() == null
                )
        );
    }

    @Test
    void testSignupCustomer_BlankFirstName() {
        Customer customerInput = mockCustomerInput();
        customerInput.setFirstName("  \n");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupCustomer(customerInput));
        assertEquals("First name cannot be empty!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testSignupCustomer_BlankLastName() {
        Customer customerInput = mockCustomerInput();
        customerInput.setLastName("\n  \n");

        ValidationException exception = assertThrows(ValidationException.class, () -> authenticationService.signupCustomer(customerInput));
        assertEquals("Last name cannot be empty!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

}
