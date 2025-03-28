package ro.unibuc.hello.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.service.CustomerService;
import ro.unibuc.hello.utils.GenericControllerTest;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.*;
import static ro.unibuc.hello.utils.GameTestUtils.buildDLCForGame;
import static ro.unibuc.hello.utils.GameTestUtils.buildGame;

@EnableAspectJAutoProxy
public class CustomerControllerTest extends GenericControllerTest<CustomerController> {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @Override
    public String getEndpoint() {
        return "customers";
    }

    @Override
    public CustomerController getController() {
        return customerController;
    }

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        super.setUp();
    }

    @Test
    void testGetCustomerById_ValidId() throws Exception {
        UserEntity mockCustomer = mockCustomerAuth();
        when(customerService.getCustomer(mockCustomer.getId())).thenReturn(mockCustomer);

        performGet(null,"/{id}", mockCustomer.getId())
                .andExpect(status().isOk())
                .andExpect(matchOne(mockCustomer, CUSTOMER_FIELDS));
    }

    @Test
    void testGetCustomerById_InvalidId() throws Exception {
        final String id = "id-invalid";
        final String errorMessage = String.format("No %s found at id %s", UserEntity.Role.CUSTOMER.toString().toLowerCase(), id);
        when(customerService.getCustomer(id)).thenThrow(new NotFoundException(errorMessage));

        performGet(null, "/{id}", id)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testGetCustomerGames_ValidId() throws Exception {
        UserEntity mockCustomer = mockCustomerAuth();
        GameEntity mockGame = buildGame();
        GameEntity mockDLC = buildDLCForGame(mockGame);
        when(customerService.getGames(mockCustomer.getId())).thenReturn(List.of(mockGame, mockDLC));

        performGet(null, "/{id}/games", mockCustomer.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(matchAll(List.of(mockGame, mockDLC), GAME_FIELDS));
    }

    @Test
    void testGetCustomerGames_InvalidId() throws Exception {
        final String id = "id-invalid";
        final String errorMessage = String.format("No %s found at id %s", UserEntity.Role.CUSTOMER.toString().toLowerCase(), id);
        when(customerService.getGames(id)).thenThrow(new NotFoundException(errorMessage));

        performGet(null, "/{id}/games", id)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testGetAllCustomers() throws Exception {
        UserEntity mockCustomer = mockCustomerAuth();
        List<UserEntity> mockCustomers = List.of(mockCustomer);
        when(customerService.getAllUsers()).thenReturn(mockCustomers);

        performGet(null, "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(matchAll(mockCustomers, CUSTOMER_FIELDS));
    }

    @Test
    void testGetMyGames_Authenticated() throws Exception {
        GameEntity mockGame = buildGame();
        GameEntity mockDLC = buildDLCForGame(mockGame);
        when(customerService.getGames()).thenReturn(List.of(mockGame, mockDLC));

        performGet(getMockedAccessToken(UserEntity.Role.CUSTOMER), "/myGames")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(matchAll(List.of(mockGame, mockDLC), GAME_FIELDS));
    }

    @Test
    void testGetMyGames_Unauthenticated() throws Exception {
        performGet("/myGames")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetMyGames_AuthenticatedCustomer() throws Exception {
        performGet(getMockedAccessToken(UserEntity.Role.DEVELOPER), "/myGames")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateLoggedCustomer_Authenticated() throws Exception {
        Customer customerInput = mockUpdatedCustomerInput();
        UserEntity mockUpdatedCustomer = mockUpdatedCustomerAuth();
        when(customerService.updateLoggedUser(argThat(devInput -> devInput.equals(customerInput))))
                .thenReturn(mockUpdatedCustomer);

        performPut(customerInput, getMockedAccessToken(UserEntity.Role.CUSTOMER), "")
                .andExpect(status().isOk())
                .andExpect(matchOne(mockUpdatedCustomer, CUSTOMER_FIELDS));
    }

    @Test
    void testUpdateLoggedCustomer_AuthenticatedCustomer() throws Exception {
        performPut(mockCustomerInput(), getMockedAccessToken(UserEntity.Role.DEVELOPER), "")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateLoggedCustomer_Unauthenticated() throws Exception {
        performPut(mockCustomerInput(), null, "")
                .andExpect(status().isUnauthorized());
    }

}
