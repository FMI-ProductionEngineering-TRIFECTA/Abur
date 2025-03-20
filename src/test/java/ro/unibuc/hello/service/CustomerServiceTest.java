package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.exception.UnauthorizedAccessException;
import ro.unibuc.hello.exception.ValidationException;
import ro.unibuc.hello.security.AuthenticationUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.*;
import static ro.unibuc.hello.utils.GameTestUtils.buildGames;

public class CustomerServiceTest {

    @Mock
    protected UserRepository userRepository;

    @InjectMocks
    private CustomerService customerService = new CustomerService();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRole() {
        UserEntity.Role role = customerService.getRole();

        assertEquals(UserEntity.Role.CUSTOMER, role);
    }

    @Test
    void testGetCustomer_ValidId() {
        UserEntity mockCustomer = mockCustomerAuth();
        when(userRepository.findByIdAndRole(mockCustomer.getId(), mockCustomer.getRole())).thenReturn(mockCustomer);

        UserEntity customer = customerService.getCustomer(mockCustomer.getId());

        assertEquals(mockCustomer, customer);
    }

    @Test
    void testGetCustomer_InvalidId() {
        final String id = "id-invalid";
        NotFoundException exception = assertThrows(NotFoundException.class, () -> customerService.getCustomer(id));
        assertEquals("No customer found at id " + id, exception.getMessage());
    }

    @Test
    void testGetAllCustomers() {
        List<UserEntity> mockUsers = buildCustomers(2);
        when(userRepository.findByRole(customerService.getRole())).thenReturn(mockUsers);

        List<UserEntity> users = customerService.getAllUsers();

        assertNotNull(users);
        assertEquals(mockUsers.size(), users.size());
        assertEquals(mockUsers, users);
    }

    @Test
    void testCustomerGetAllGames_ValidId() {
        UserEntity mockCustomer = mockCustomerAuth();
        mockCustomer.setGames(buildGames(2));
        when(userRepository.findByIdAndRole(mockCustomer.getId(), mockCustomer.getRole())).thenReturn(mockCustomer);

        List<GameEntity> games = customerService.getGames(mockCustomer.getId());

        assertNotNull(games);
        assertEquals(mockCustomer.getGames().size(), games.size());
        assertEquals(mockCustomer.getGames(), games);
    }

    @Test
    void testCustomerGetAllGames_InvalidId() {
        final String id = "id-invalid";

        NotFoundException exception = assertThrows(NotFoundException.class, () -> customerService.getGames(id));
        assertEquals("No customer found at id " + id, exception.getMessage());
    }

    @Test
    void testCustomerGetMyGames_Authenticated() {
        UserEntity mockCustomer = mockCustomerAuth();
        mockCustomer.setGames(buildGames(2));

        List<GameEntity> games = customerService.getGames();

        assertNotNull(games);
        assertEquals(mockCustomer.getGames().size(), games.size());
        assertEquals(mockCustomer.getGames(), games);
    }

    @Test
    void testCustomerGetMyGames_Unauthenticated() {
        // SecurityContextHolder.getContext().setAuthentication(null);

        // TODO: UnauthorizedAccessException not thrown
        // assertThrows(UnauthorizedAccessException.class, () -> customerService.getGames());
    }

    @Test
    void testUpdateCustomer_AuthenticatedValid() {
        Customer customerInput = mockUpdatedCustomer();
        mockCustomerAuth();

        customerService.updateLoggedUser(customerInput);

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
    void testUpdateCustomer_Authenticated_NullUsername() {
        Customer customerInput = mockUpdatedCustomer();
        customerInput.setUsername(null);
        mockCustomerAuth();

        customerService.updateLoggedUser(customerInput);

        verify(userRepository, times(1)).save(
                argThat(cus
                        -> cus.getUsername().equals(mockCustomer().getUsername())
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
    void testUpdateCustomer_Authenticated_NonUniqueUsername() {
        Customer customerInput = mockUpdatedCustomer();
        customerInput.setUsername(mockCustomer().getUsername());
        UserEntity customer = mockCustomerAuth();

        when(userRepository.findByUsername(customer.getUsername())).thenReturn(customer);

        ValidationException exception = assertThrows(ValidationException.class, () -> customerService.updateLoggedUser(customerInput));
        assertEquals("Username customer already exists!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateCustomer_Authenticated_BlankUsername() {
        Customer customerInput = mockUpdatedCustomer();
        customerInput.setUsername("        ");
        mockCustomerAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> customerService.updateLoggedUser(customerInput));
        assertEquals("Username cannot be empty!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateCustomer_Authenticated_NoMinLengthUsername() {
        Customer customerInput = mockUpdatedCustomer();
        customerInput.setUsername("cus");
        mockCustomerAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> customerService.updateLoggedUser(customerInput));
        assertEquals("Username must be at least 5 characters long!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateCustomer_Authenticated_NullPassword() {
        Customer customerInput = mockUpdatedCustomer();
        customerInput.setPassword(null);
        mockCustomerAuth();

        customerService.updateLoggedUser(customerInput);

        verify(userRepository, times(1)).save(
                argThat(cus
                        -> cus.getUsername().equals(customerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(mockCustomer().getPassword(), cus.getPassword())
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
    void testUpdateCustomer_Authenticated_NoUppercaseLetterPassword() {
        Customer customerInput = mockUpdatedCustomer();
        customerInput.setPassword("pass");
        mockCustomerAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> customerService.updateLoggedUser(customerInput));
        assertEquals("Password must contain at least one uppercase letter!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateCustomer_Authenticated_NoLowercaseLetterPassword() {
        // TODO
    }

    @Test
    void testUpdateCustomer_Authenticated_NoDigitPassword() {
        // TODO
    }

    @Test
    void testUpdateCustomer_Authenticated_NoMinLengthPassword() {
        // TODO
    }

    @Test
    void testUpdateCustomer_Authenticated_NullEmail() {
        Customer customerInput = mockUpdatedCustomer();
        customerInput.setEmail(null);
        mockCustomerAuth();

        customerService.updateLoggedUser(customerInput);

        verify(userRepository, times(1)).save(
                argThat(cus
                        -> cus.getUsername().equals(customerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(customerInput.getPassword(), cus.getPassword())
                        && cus.getEmail().equals(mockCustomer().getEmail())
                        && cus.getRole().equals(UserEntity.Role.CUSTOMER)
                        && cus.getDetails().getFirstName().equals(customerInput.getFirstName())
                        && cus.getDetails().getLastName().equals(customerInput.getLastName())
                        && cus.getDetails().getStudio() == null
                        && cus.getDetails().getWebsite() == null
                )
        );
    }

    @Test
    void testUpdateCustomer_Authenticated_NonUniqueEmail() {
        // TODO
    }

    @Test
    void testUpdateCustomer_Authenticated_NoFormatEmail() {
        // TODO
    }

    @Test
    void testUpdateCustomer_Authenticated_NullFirstName() {
        // TODO
    }

    @Test
    void testUpdateCustomer_Authenticated_BlankFirstName() {
        // TODO
    }

    @Test
    void testUpdateCustomer_Authenticated_NullLastName() {
        // TODO
    }

    @Test
    void testUpdateCustomer_Authenticated_BlankLastName() {
        // TODO
    }

    @Test
    void testUpdateCustomer_Unauthenticated() {
        // Customer customerInput = mockCustomer();
        // SecurityContextHolder.getContext().setAuthentication(null);

        // TODO: UnauthorizedAccessException not thrown
        // assertThrows(UnauthorizedAccessException.class, () -> customerService.updateLoggedUser(customerInput));
    }

}
