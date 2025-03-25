package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.exception.NotFoundException;
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
        resetAccessToken();
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
    void testUpdateCustomer_AuthenticatedValid() {
        Customer customerInput = mockUpdatedCustomerInput();
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
        Customer customerInput = mockUpdatedCustomerInput();
        customerInput.setUsername(null);
        UserEntity mockCustomer = mockCustomerAuth();

        customerService.updateLoggedUser(customerInput);

        verify(userRepository, times(1)).save(
                argThat(cus
                        -> cus.getUsername().equals(mockCustomer.getUsername())
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
        UserEntity mockCustomer = mockCustomerAuth();
        Customer customerInput = mockUpdatedCustomerInput();
        customerInput.setUsername(mockCustomer.getUsername());

        when(userRepository.findByUsername(mockCustomer.getUsername())).thenReturn(mockCustomer);

        ValidationException exception = assertThrows(ValidationException.class, () -> customerService.updateLoggedUser(customerInput));
        assertEquals("Username " + customerInput.getUsername() + " already exists!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateCustomer_Authenticated_BlankUsername() {
        Customer customerInput = mockUpdatedCustomerInput();
        customerInput.setUsername("        ");
        mockCustomerAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> customerService.updateLoggedUser(customerInput));
        assertEquals("Username cannot be empty!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateCustomer_Authenticated_NoMinLengthUsername() {
        Customer customerInput = mockUpdatedCustomerInput();
        customerInput.setUsername("cus");
        mockCustomerAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> customerService.updateLoggedUser(customerInput));
        assertEquals("Username must be at least 5 characters long!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateCustomer_Authenticated_NullPassword() {
        Customer customerInput = mockUpdatedCustomerInput();
        customerInput.setPassword(null);
        mockCustomerAuth();

        customerService.updateLoggedUser(customerInput);

        verify(userRepository, times(1)).save(
                argThat(cus
                        -> cus.getUsername().equals(customerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(mockCustomerInput().getPassword(), cus.getPassword())
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
        Customer customerInput = mockUpdatedCustomerInput();
        customerInput.setPassword("pass");
        mockCustomerAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> customerService.updateLoggedUser(customerInput));
        assertEquals("Password must contain at least one uppercase letter!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateCustomer_Authenticated_NullEmail() {
        Customer customerInput = mockUpdatedCustomerInput();
        customerInput.setEmail(null);
        UserEntity mockCustomer = mockCustomerAuth();

        customerService.updateLoggedUser(customerInput);

        verify(userRepository, times(1)).save(
                argThat(cus
                        -> cus.getUsername().equals(customerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(customerInput.getPassword(), cus.getPassword())
                        && cus.getEmail().equals(mockCustomer.getEmail())
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
        UserEntity mockCustomer = mockCustomerAuth();
        Customer customerInput = mockUpdatedCustomerInput();
        customerInput.setEmail(mockCustomer.getEmail());

        when(userRepository.findByEmail(mockCustomer.getEmail())).thenReturn(mockCustomer);

        ValidationException exception = assertThrows(ValidationException.class, () -> customerService.updateLoggedUser(customerInput));
        assertEquals("Email " + customerInput.getEmail() + " already exists!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateCustomer_Authenticated_NoFormatEmail() {
        Customer customerInput = mockUpdatedCustomerInput();
        customerInput.setEmail("invalid-format");
        mockCustomerAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> customerService.updateLoggedUser(customerInput));
        assertEquals("Email must be a valid email address!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateCustomer_Authenticated_NullFirstName() {
        Customer customerInput = mockUpdatedCustomerInput();
        customerInput.setFirstName(null);
        UserEntity mockCustomer = mockCustomerAuth();

        customerService.updateLoggedUser(customerInput);

        verify(userRepository, times(1)).save(
                argThat(cus
                        -> cus.getUsername().equals(customerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(customerInput.getPassword(), cus.getPassword())
                        && cus.getEmail().equals(customerInput.getEmail())
                        && cus.getRole().equals(UserEntity.Role.CUSTOMER)
                        && cus.getDetails().getFirstName().equals(mockCustomer.getDetails().getFirstName())
                        && cus.getDetails().getLastName().equals(customerInput.getLastName())
                        && cus.getDetails().getStudio() == null
                        && cus.getDetails().getWebsite() == null
                )
        );
    }

    @Test
    void testUpdateCustomer_Authenticated_BlankFirstName() {
        Customer customerInput = mockUpdatedCustomerInput();
        customerInput.setFirstName("   \n    ");
        mockCustomerAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> customerService.updateLoggedUser(customerInput));
        assertEquals("First name cannot be empty!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateCustomer_Authenticated_NullLastName() {
        Customer customerInput = mockUpdatedCustomerInput();
        customerInput.setLastName(null);
        UserEntity mockCustomer = mockCustomerAuth();

        customerService.updateLoggedUser(customerInput);

        verify(userRepository, times(1)).save(
                argThat(cus
                        -> cus.getUsername().equals(customerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(customerInput.getPassword(), cus.getPassword())
                        && cus.getEmail().equals(customerInput.getEmail())
                        && cus.getRole().equals(UserEntity.Role.CUSTOMER)
                        && cus.getDetails().getFirstName().equals(customerInput.getFirstName())
                        && cus.getDetails().getLastName().equals(mockCustomer.getDetails().getLastName())
                        && cus.getDetails().getStudio() == null
                        && cus.getDetails().getWebsite() == null
                )
        );
    }

    @Test
    void testUpdateCustomer_Authenticated_BlankLastName() {
        Customer customerInput = mockUpdatedCustomerInput();
        customerInput.setLastName("   \t    ");
        mockCustomerAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> customerService.updateLoggedUser(customerInput));
        assertEquals("Last name cannot be empty!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

}
