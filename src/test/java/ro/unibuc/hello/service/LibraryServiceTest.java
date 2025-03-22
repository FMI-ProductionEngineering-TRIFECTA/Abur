package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.*;
import ro.unibuc.hello.exception.NotFoundException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.*;
import static ro.unibuc.hello.utils.GameTestUtils.*;

public class LibraryServiceTest {

    @Mock
    protected LibraryRepository libraryRepository;

    @InjectMocks
    protected LibraryService libraryService;

    @Mock
    protected CustomerService customerService;

    private static final String notFoundFormat = "No customer found at id %s!";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetLibrary_Valid() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(3);
        customer.getGames().addAll(games);
        when(libraryService.getLibraryByCustomerId(customer.getId())).thenReturn(customer.getGames());

        List<GameEntity> response = libraryService.getLibrary();
        assertNotNull(response);
        assertEquals(customer.getGames().size(), response.size());
        assertEquals(customer.getGames(), response);
    }

    @Test
    void testGetLibraryById_Valid() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(3);
        customer.getGames().addAll(games);
        when(customerService.getCustomer(customer.getId())).thenReturn(customer);
        when(libraryRepository.getGamesByCustomer(customer)).thenReturn(customer.getGames());

        List<GameEntity> response = libraryService.getLibraryByCustomerId(customer.getId());
        assertNotNull(response);
        assertEquals(customer.getGames().size(), response.size());
        assertEquals(customer.getGames(), response);
    }

    @Test
    void testGetLibraryById_InvalidId() {
        UserEntity customer = mockCustomerAuth();
        when(customerService.getCustomer(customer.getId()))
                .thenThrow(new NotFoundException(notFoundFormat, customer.getId()));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> libraryService.getLibraryByCustomerId(customer.getId())
        );
        assertNotNull(exception);
        assertEquals(String.format(notFoundFormat, customer.getId()), exception.getMessage());
    }
}
