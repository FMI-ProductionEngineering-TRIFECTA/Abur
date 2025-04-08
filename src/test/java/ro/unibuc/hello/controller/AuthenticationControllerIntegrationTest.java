package ro.unibuc.hello.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.TestcontainersConfiguration;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Credentials;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.dto.Token;
import ro.unibuc.hello.utils.GenericControllerIntegrationTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.mockCustomerInput;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.mockDeveloperInput;

public class AuthenticationControllerIntegrationTest extends GenericControllerIntegrationTest<AuthenticationController> {

    static {
        TestcontainersConfiguration
                .getInstance()
                .updateUserConfig("ryuk.container.privileged", "true");
    }

    @Container
    private final static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.20")
            .withExposedPorts(27017)
            .withSharding();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("mongodb.connection.url", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationController authenticationController;

    @Override
    public String getEndpoint() {
        return "auth";
    }

    @Override
    public AuthenticationController getController() {
        return authenticationController;
    }

    @Test
    void testLogin_ValidCredentials() throws Exception {
        UserEntity customer = userRepository.findByIdAndRole(getUserId(Role.CUSTOMER), Role.CUSTOMER);
        Credentials credentials = new Credentials(customer.getUsername(), String.format("%s1234", customer.getUsername()));
        Token token = new Token(getAccessToken(UserEntity.Role.CUSTOMER));

        performPost(credentials, null, "/login")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token.getToken()));
    }

    @Test
    void testLogin_InvalidUsername() throws Exception {
        Credentials credentials = new Credentials("customer-invalid", "customer123-PASSWORD");

        performPost(credentials, null, "/login")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void testLogin_NullUsername() throws Exception {
        Credentials credentials = new Credentials(null, "customer123-PASSWORD");

        performPost(credentials, null, "/login")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username is required"));
    }

    @Test
    void testLogin_BlankUsername() throws Exception {
        Credentials credentials = new Credentials("   ", "customer123-PASSWORD");

        performPost(credentials, null, "/login")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username cannot be blank"));
    }

    @Test
    void testLogin_InvalidPassword() throws Exception {
        Credentials credentials = new Credentials("customer", "customer123-invalid");

        performPost(credentials, null, "/login")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void testLogin_NullPassword() throws Exception {
        Credentials credentials = new Credentials("customer", null);

        performPost(credentials, null, "/login")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password is required"));
    }

    @Test
    void testLogin_BlankPassword() throws Exception {
        Credentials credentials = new Credentials("customer", "\n\n\n");

        performPost(credentials, null, "/login")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password cannot be blank"));
    }

    @Test
    void testSignupDeveloper_ValidInput() throws Exception {
        Developer developerInput = mockDeveloperInput();
        List<UserEntity> oldDevelopersDB = userRepository.findAllByRole(UserEntity.Role.DEVELOPER);

        ResultActions newDeveloperResults = performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isCreated());

        List<UserEntity> newDevelopersDB = userRepository.findAllByRole(UserEntity.Role.DEVELOPER);
        UserEntity newDeveloperDB = newDevelopersDB.stream()
                .filter(dev -> !oldDevelopersDB.contains(dev))
                .findFirst()
                .orElse(null);

        assertNotNull(newDeveloperDB);

        newDeveloperResults
                .andExpect(matchOne(newDeveloperDB, DEVELOPER_FIELDS));
    }

    @Test
    void testSignupCustomer_ValidInput() throws Exception {
        Customer customerInput = mockCustomerInput();
        List<UserEntity> oldCustomersDB = userRepository.findAllByRole(Role.CUSTOMER);

        ResultActions newCustomerResults = performPost(customerInput, null, "/signup/customer")
                .andExpect(status().isCreated());

        List<UserEntity> newCustomersDB = userRepository.findAllByRole(UserEntity.Role.CUSTOMER);
        UserEntity newCustomerDB = newCustomersDB.stream()
                .filter(dev -> !oldCustomersDB.contains(dev))
                .findFirst()
                .orElse(null);

        assertNotNull(newCustomerDB);

        newCustomerResults
                .andExpect(matchOne(newCustomerDB, CUSTOMER_FIELDS));
    }

    @Test
    void testSignupDeveloper_NullUsername() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setUsername(null);

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username is required"));
    }

    @Test
    void testSignupDeveloper_BlankUsername() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setUsername("           ");

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username cannot be blank"));
    }

    @Test
    void testSignupDeveloper_NonUniqueUsername() throws Exception {
        UserEntity developer = userRepository.findByIdAndRole(getUserId(Role.DEVELOPER), Role.DEVELOPER);
        Developer developerInput = mockDeveloperInput();
        developerInput.setUsername(developer.getUsername());

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(String.format("Username %s already exists!", developerInput.getUsername())));
    }

    @Test
    void testSignupDeveloper_NoMinLengthUsername() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setUsername("dev");

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username must be at least " + MIN_STRING_VALID_LENGTH + " characters long!"));
    }

    @Test
    void testSignupDeveloper_NullPassword() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setPassword(null);

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password is required"));
    }

    @Test
    void testSignupDeveloper_BlankPassword() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setPassword("");

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password cannot be blank"));
    }

    @Test
    void testSignupDeveloper_NoUppercaseLetterPassword() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setPassword("pass");

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password must contain at least one uppercase letter!"));
    }

    @Test
    void testSignupDeveloper_NoLowercaseLetterPassword() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setPassword("PASS");

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password must contain at least one lowercase letter!"));
    }

    @Test
    void testSignupDeveloper_NoDigitPassword() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setPassword("Pass");

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password must contain at least one digit!"));
    }

    @Test
    void testSignupDeveloper_NoMinLengthPassword() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setPassword("Pa1");

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password must be at least " + MIN_STRING_VALID_LENGTH + " characters long!"));
    }

    @Test
    void testSignupDeveloper_NullEmail() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setEmail(null);

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email is required"));
    }

    @Test
    void testSignupDeveloper_BlankEmail() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setEmail(" \n\t");

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email cannot be blank"));
    }

    @Test
    void testSignupDeveloper_NonUniqueEmail() throws Exception {
        UserEntity developer = userRepository.findByIdAndRole(getUserId(Role.DEVELOPER), Role.DEVELOPER);
        Developer developerInput = mockDeveloperInput();
        developerInput.setEmail(developer.getEmail());

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(String.format("Email %s already exists!", developerInput.getEmail())));
    }

    @Test
    void testSignupDeveloper_NoFormatEmail() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setEmail("mail");

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email must be a valid email address!"));
    }

    @Test
    void testSignupDeveloper_NullStudio() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setStudio(null);

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Studio is required"));
    }

    @Test
    void testSignupDeveloper_BlankStudio() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setStudio("\n");

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Studio cannot be blank"));
    }

    @Test
    void testSignupDeveloper_NonUniqueStudio() throws Exception {
        UserEntity developer = userRepository.findByIdAndRole(getUserId(Role.DEVELOPER), Role.DEVELOPER);
        Developer developerInput = mockDeveloperInput();
        developerInput.setStudio(developer.getDetails().getStudio());

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(String.format("Studio %s already exists!", developerInput.getStudio())));
    }

    @Test
    void testSignupDeveloper_NoFormatWebsite() throws Exception {
        Developer developerInput = mockDeveloperInput();
        developerInput.setWebsite("developer-website");

        performPost(developerInput, null, "/signup/developer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Website must be a valid website URL!"));
    }

    @Test
    void testSignupCustomer_BlankFirstName() throws Exception {
        Customer customerInput = mockCustomerInput();
        customerInput.setFirstName("  \n");

        performPost(customerInput, null, "/signup/customer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("First name cannot be empty!"));
    }

    @Test
    void testSignupCustomer_BlankLastName() throws Exception {
        Customer customerInput = mockCustomerInput();
        customerInput.setLastName("\n  \n");

        performPost(customerInput, null, "/signup/customer")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Last name cannot be empty!"));
    }

}
