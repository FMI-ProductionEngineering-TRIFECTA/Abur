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
import static ro.unibuc.hello.utils.AuthenticationTestUtils.getAccessToken;
import static ro.unibuc.hello.utils.GameTestUtils.buildDLCForGame;
import static ro.unibuc.hello.utils.GameTestUtils.buildGame;

@EnableAspectJAutoProxy
public class CustomerControllerTest extends GenericControllerTest<CustomerController> {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @Override
    protected String getEndpoint() {
        return "customers";
    }

    @Override
    protected CustomerController getController() {
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
                .andExpect(jsonPath("$.id").value(mockCustomer.getId()))
                .andExpect(jsonPath("$.username").value(mockCustomer.getUsername()))
                .andExpect(jsonPath("$.password").value(mockCustomer.getPassword()))
                .andExpect(jsonPath("$.email").value(mockCustomer.getEmail()))
                .andExpect(jsonPath("$.details.firstName").value(mockCustomer.getDetails().getFirstName()))
                .andExpect(jsonPath("$.details.lastName").value(mockCustomer.getDetails().getLastName()))
                .andExpect(jsonPath("$.details.studio").doesNotExist())
                .andExpect(jsonPath("$.details.website").doesNotExist());
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
                    .andExpect(jsonPath("$[0].id").value(mockGame.getId()))
                    .andExpect(jsonPath("$[0].title").value(mockGame.getTitle()))
                    .andExpect(jsonPath("$[0].price").value(mockGame.getPrice()))
                    .andExpect(jsonPath("$[0].discountPercentage").value(mockGame.getDiscountPercentage()))
                    .andExpect(jsonPath("$[0].keys").value(mockGame.getKeys()))
                    .andExpect(jsonPath("$[0].type").value(mockGame.getType().toString()))

                    .andExpect(jsonPath("$[1].id").value(mockDLC.getId()))
                    .andExpect(jsonPath("$[1].title").value(mockDLC.getTitle()))
                    .andExpect(jsonPath("$[1].price").value(mockDLC.getPrice()))
                    .andExpect(jsonPath("$[1].discountPercentage").value(mockDLC.getDiscountPercentage()))
                    .andExpect(jsonPath("$[1].keys").value(mockDLC.getKeys()))
                    .andExpect(jsonPath("$[1].type").value(mockDLC.getType().toString()));
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
        when(customerService.getAllUsers()).thenReturn(List.of(mockCustomer));

        performGet(null, "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(mockCustomer.getId()))
                    .andExpect(jsonPath("$[0].username").value(mockCustomer.getUsername()))
                    .andExpect(jsonPath("$[0].password").value(mockCustomer.getPassword()))
                    .andExpect(jsonPath("$[0].email").value(mockCustomer.getEmail()))
                    .andExpect(jsonPath("$[0].details.firstName").value(mockCustomer.getDetails().getFirstName()))
                    .andExpect(jsonPath("$[0].details.lastName").value(mockCustomer.getDetails().getLastName()))
                    .andExpect(jsonPath("$[0].details.studio").doesNotExist())
                    .andExpect(jsonPath("$[0].details.website").doesNotExist());
    }

    @Test
    void testGetMyGames_Authenticated() throws Exception {
        GameEntity mockGame = buildGame();
        GameEntity mockDLC = buildDLCForGame(mockGame);
        when(customerService.getGames()).thenReturn(List.of(mockGame, mockDLC));

        performGet(getAccessToken(UserEntity.Role.CUSTOMER), "/myGames")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(mockGame.getId()))
                    .andExpect(jsonPath("$[0].title").value(mockGame.getTitle()))
                    .andExpect(jsonPath("$[0].price").value(mockGame.getPrice()))
                    .andExpect(jsonPath("$[0].discountPercentage").value(mockGame.getDiscountPercentage()))
                    .andExpect(jsonPath("$[0].keys").value(mockGame.getKeys()))
                    .andExpect(jsonPath("$[0].type").value(mockGame.getType().toString()))

                    .andExpect(jsonPath("$[1].id").value(mockDLC.getId()))
                    .andExpect(jsonPath("$[1].title").value(mockDLC.getTitle()))
                    .andExpect(jsonPath("$[1].price").value(mockDLC.getPrice()))
                    .andExpect(jsonPath("$[1].discountPercentage").value(mockDLC.getDiscountPercentage()))
                    .andExpect(jsonPath("$[1].keys").value(mockDLC.getKeys()))
                    .andExpect(jsonPath("$[1].type").value(mockDLC.getType().toString()));
    }

    @Test
    void testGetMyGames_Unauthenticated() throws Exception {
        performGet("/myGames")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetMyGames_AuthenticatedCustomer() throws Exception {
        performGet(getAccessToken(UserEntity.Role.DEVELOPER), "/myGames")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateLoggedCustomer_Authenticated() throws Exception {
        Customer customerInput = mockUpdatedCustomerInput();
        UserEntity mockUpdatedCustomer = mockUpdatedCustomerAuth();
        when(customerService.updateLoggedUser(argThat(devInput -> devInput.equals(customerInput))))
                .thenReturn(mockUpdatedCustomer);

        performPut(customerInput, getAccessToken(UserEntity.Role.CUSTOMER), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockUpdatedCustomer.getId()))
                .andExpect(jsonPath("$.username").value(customerInput.getUsername()))
                .andExpect(jsonPath("$.password").value(mockUpdatedCustomer.getPassword()))
                .andExpect(jsonPath("$.email").value(customerInput.getEmail()))
                .andExpect(jsonPath("$.details.firstName").value(customerInput.getFirstName()))
                .andExpect(jsonPath("$.details.lastName").value(customerInput.getLastName()))
                .andExpect(jsonPath("$.details.studio").doesNotExist())
                .andExpect(jsonPath("$.details.website").doesNotExist());
    }

    @Test
    void testUpdateLoggedCustomer_AuthenticatedCustomer() throws Exception {
        performPut(mockCustomerInput(), getAccessToken(UserEntity.Role.DEVELOPER), "")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateLoggedCustomer_Unauthenticated() throws Exception {
        performPut(mockCustomerInput(), null, "")
                .andExpect(status().isUnauthorized());
    }

}
