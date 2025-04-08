package ro.unibuc.hello.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.utils.GenericControllerIntegrationTest;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.mockCustomerInput;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.mockUpdatedCustomerInput;

public class CustomerControllerIntegrationTest extends GenericControllerIntegrationTest<CustomerController> {

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
    private CustomerController customerController;

    @Override
    public String getEndpoint() {
        return "customers";
    }

    @Override
    public CustomerController getController() {
        return customerController;
    }

    @Test
    void testGetCustomerById_ValidId() throws Exception {
        UserEntity customerDB = userRepository.findByIdAndRole(getUserId(Role.CUSTOMER), Role.CUSTOMER);

        performGet(null,"/{id}", customerDB.getId())
                .andExpect(status().isOk())
                .andExpect(matchOne(customerDB, CUSTOMER_FIELDS));
    }

    @Test
    void testGetCustomerById_InvalidId() throws Exception {
        final String id = "id-invalid";

        performGet(null, "/{id}", id)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(String.format("No %s found at id %s", Role.CUSTOMER.toString().toLowerCase(), id)));
    }

    @Test
    void testGetCustomerGames_ValidId() throws Exception {
        UserEntity customerDB = userRepository.findByIdAndRole(getUserId(Role.CUSTOMER), Role.CUSTOMER);

        performGet(null, "/{id}/games", customerDB.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(customerDB.getGames().size())))
                .andExpect(matchAll(customerDB.getGames(), GAME_FIELDS));
    }

    @Test
    void testGetCustomerGames_InvalidId() throws Exception {
        final String id = "id-invalid";

        performGet(null, "/{id}/games", id)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(String.format("No %s found at id %s", Role.CUSTOMER.toString().toLowerCase(), id)));
    }

    @Test
    void testGetAllCustomers() throws Exception {
        List<UserEntity> customersDB = userRepository.findAllByRole(Role.CUSTOMER);

        performGet(null, "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(customersDB.size())))
                .andExpect(matchAll(customersDB, CUSTOMER_FIELDS));
    }

    @Test
    void testGetMyGames_Authenticated() throws Exception {
        UserEntity customerDB = userRepository.findByIdAndRole(getUserId(Role.CUSTOMER), Role.CUSTOMER);

        performGet(getAccessToken(Role.CUSTOMER), "/myGames")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(customerDB.getGames().size())))
                .andExpect(matchAll(customerDB.getGames(), GAME_FIELDS));
    }

    @Test
    void testGetMyGames_Unauthenticated() throws Exception {
        performGet("/myGames")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetMyGames_AuthenticatedDeveloper() throws Exception {
        performGet(getAccessToken(Role.DEVELOPER), "/myGames")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateLoggedCustomer_Authenticated() throws Exception {
        Customer customerInput = mockUpdatedCustomerInput();
        UserEntity customerDB = userRepository.findByIdAndRole(getUserId(Role.CUSTOMER), Role.CUSTOMER);

        performPut(customerInput, getAccessToken(Role.CUSTOMER), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerDB.getId()))
                .andExpect(jsonPath("$.username").value(customerInput.getUsername()))
                .andExpect(jsonPath("$.email").value(customerInput.getEmail()))
                .andExpect(jsonPath("$.details.firstName").value(customerInput.getFirstName()))
                .andExpect(jsonPath("$.details.lastName").value(customerInput.getLastName()))
                .andExpect(jsonPath("$.details.studio").doesNotExist())
                .andExpect(jsonPath("$.details.website").doesNotExist());
    }

    @Test
    void testUpdateLoggedCustomer_AuthenticatedDeveloper() throws Exception {
        performPut(mockCustomerInput(), getAccessToken(Role.DEVELOPER), "")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateLoggedCustomer_Unauthenticated() throws Exception {
        performPut(mockCustomerInput(), null, "")
                .andExpect(status().isUnauthorized());
    }

}
