package ro.unibuc.hello.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.data.repository.LibraryRepository;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.exception.UnauthorizedAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static ro.unibuc.hello.security.AuthenticationUtils.getUser;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.mockCustomerAuth;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.resetMockedAccessToken;
import static ro.unibuc.hello.utils.GameTestUtils.buildGames;

public class StoreServiceTest {

    @Mock
    protected GameRepository gameRepository;

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MeterRegistry metricsRegistry;

    @InjectMocks
    protected StoreService storeService;

    private static Boolean hideOwned = false;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resetMockedAccessToken();
    }

    @Test
    void testGetStore_AllGames()  {
        hideOwned = false;
        List<GameEntity> games = buildGames(3);
        Counter counterMock = Mockito.mock(Counter.class);

        when(metricsRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        when(gameRepository.findAll()).thenReturn(games);

        List<GameEntity> response = storeService.getStore(hideOwned);
        assertNotNull(response);
        assertEquals(games.size(), response.size());
        assertEquals(games, response);
    }

    @Test
    void testGetStore_HideOwned() {
        hideOwned = true;
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(3);
        List<GameEntity> owned_games = List.of(games.get(0));
        List<GameEntity> unowned_games = new ArrayList<>(games);
        unowned_games.removeAll(owned_games);
        Counter counterMock = Mockito.mock(Counter.class);

        when(metricsRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        when(gameRepository.findAll()).thenReturn(games);
        when(userRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(libraryRepository.getGamesByCustomer(getUser())).thenReturn(owned_games);

        List<GameEntity> response = storeService.getStore(hideOwned);
        assertNotNull(response);
        assertEquals(unowned_games.size(), response.size());
        assertEquals(unowned_games, response);
    }

    @Test
    void testGetStore_HideOwned_NoAuth() {
        hideOwned = true;
        List<GameEntity> games = buildGames(3);
        Counter counterMock = Mockito.mock(Counter.class);

        when(metricsRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        when(gameRepository.findAll()).thenReturn(games);
        when(userRepository.findById(any())).thenThrow(new UnauthorizedAccessException());

        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> storeService.getStore(hideOwned)
        );
        assertNotNull(exception);
        assertEquals(UnauthorizedAccessException.class, exception.getClass());
    }
}
