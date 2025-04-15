package ro.unibuc.hello.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ro.unibuc.hello.data.entity.CartEntity;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.LibraryEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.CartRepository;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.data.repository.LibraryRepository;
import ro.unibuc.hello.data.repository.WishlistRepository;
import ro.unibuc.hello.dto.CartInfo;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.unibuc.hello.data.entity.CartEntity.buildCartEntry;
import static ro.unibuc.hello.data.entity.GameEntity.totalPrice;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.mockCustomerAuth;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.resetMockedAccessToken;
import static ro.unibuc.hello.utils.DatabaseUtils.CompositeKey;
import static ro.unibuc.hello.utils.GameTestUtils.buildGame;
import static ro.unibuc.hello.utils.GameTestUtils.buildGames;

class CartServiceTest {

    @Mock
    protected LibraryRepository libraryRepository;

    @Mock
    protected WishlistRepository wishlistRepository;

    @Mock
    protected CartRepository cartRepository;

    @Mock
    protected GameRepository gameRepository;

    @Mock
    protected CustomerService customerService;

    @Mock
    protected GameService gameService;

    @Mock
    private MeterRegistry metricsRegistry;

    @InjectMocks
    private CartService cartService;

    private static final String notFoundFormat = "No customer found at id %s!";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resetMockedAccessToken();
    }

    @Test
    void testGetCart_Valid() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(3);
        Counter counterMock = Mockito.mock(Counter.class);

        when(metricsRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        when(customerService.getCustomer(customer.getId())).thenReturn(customer);
        when(cartRepository.getGamesByCustomer(customer)).thenReturn(games);

        CartInfo response = cartService.getCart();
        assertNotNull(response);
        assertEquals(response.getPrice(), totalPrice(games));
        assertEquals(response.getItems().size(), games.size());
        assertEquals(response.getItems().get(0), games.get(0));
        assertEquals(response.getItems().get(1), games.get(1));
        assertEquals(response.getItems().get(2), games.get(2));
    }

    @Test
    void testGetCart_InvalidId() {
        UserEntity customer = mockCustomerAuth();
        String customerId = customer.getId();
        Counter counterMock = Mockito.mock(Counter.class);

        when(metricsRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        when(customerService.getCustomer(customerId))
                .thenThrow(new NotFoundException(notFoundFormat, customerId));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> cartService.getCart()
        );
        assertNotNull(exception);
        assertEquals(String.format(notFoundFormat, customerId), exception.getMessage());
    }

    @Test
    void testCheckout_Valid() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(6);
        List<Integer> numKeyList = games
                .stream()
                .map(GameEntity::getKeys)
                .toList();
        List<GameEntity> customerGames = customer.getGames();
        customerGames.add(games.get(0));
        customerGames.add(games.get(1));
        customerGames.add(games.get(2));
        games.removeAll(customerGames);
        int initialNumGames = customerGames.size();
        Counter counterMock = Mockito.mock(Counter.class);

        ArgumentCaptor<GameEntity> gameCaptor = ArgumentCaptor.forClass(GameEntity.class);
        ArgumentCaptor<LibraryEntity> libraryCaptor = ArgumentCaptor.forClass(LibraryEntity.class);
        ArgumentCaptor<CompositeKey> wishlistCaptor = ArgumentCaptor.forClass(CompositeKey.class);

        when(metricsRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        when(cartRepository.getGamesByCustomer(customer)).thenReturn(games);
        when(gameRepository.save(any(GameEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(libraryRepository.save(any(LibraryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(wishlistRepository).deleteById(any(CompositeKey.class));

        cartService.checkout();

        // capture the arguments and then get the values
        verify(gameRepository, times(games.size())).save(gameCaptor.capture());
        List<GameEntity> capturedGameEntities = gameCaptor.getAllValues();

        verify(libraryRepository, times(games.size())).save(libraryCaptor.capture());
        List<LibraryEntity> capturedLibraryEntities = libraryCaptor.getAllValues();

        verify(wishlistRepository, times(games.size())).deleteById(wishlistCaptor.capture());
        List<CompositeKey> capturedCompositeKeys = wishlistCaptor.getAllValues();

        assertEquals(games.size(), capturedGameEntities.size());
        assertEquals(games.size(), capturedLibraryEntities.size());
        assertEquals(games.size(), capturedCompositeKeys.size());
        assertEquals(customer.getGames().size(), initialNumGames + games.size());

        for (int i = 0; i < games.size(); i++) {
            GameEntity game = games.get(i);

            assertEquals(max(numKeyList.get(i) - 1, 0), game.getKeys());

            assertNotNull(capturedGameEntities.get(i));
            assertEquals(game.getId(), capturedGameEntities.get(i).getId());

            assertNotNull(capturedLibraryEntities.get(i));
            assertEquals(game.getId(), capturedLibraryEntities.get(i).getGame().getId());

            assertNotNull(capturedCompositeKeys.get(i));
            assertEquals(game.getId(), capturedCompositeKeys.get(i).getGameId());
            assertEquals(customer.getId(), capturedCompositeKeys.get(i).getCustomerId());
        }
    }

    @Test
    void testCheckout_NotEnoughKeys() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(3);
        String notEnoughKeysFormat = "There are no more keys for %s, please remove it from the cart!";
        games.get(2).setKeys(0);
        Counter counterMock = Mockito.mock(Counter.class);

        when(metricsRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        when(cartRepository.getGamesByCustomer(customer)).thenReturn(games);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> cartService.checkout()
        );
        assertNotNull(exception);
        assertEquals(String.format(notEnoughKeysFormat, games.get(2).getTitle()), exception.getMessage());
    }

    @Test
    void testAddToCart_Valid() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(3);
        GameEntity game = games.get(0);
        games.remove(game);
        CartEntity cartEntry = buildCartEntry(game, customer);

        when(gameService.getGame(game.getId())).thenReturn(game);
        when(cartRepository.getGamesByCustomer(customer)).thenReturn(games);
        when(libraryRepository.getGamesByCustomer(customer)).thenReturn(games);
        when(cartRepository.save(cartEntry)).thenReturn(cartEntry);

        CartEntity response = cartService.addToCart(game.getId());
        assertNotNull(response);
        assertEquals(response, cartEntry);
        verify(cartRepository, times(1)).save(cartEntry);
    }

    @Test
    void testAddToCart_NotEnoughKeys() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(3);
        String notEnoughKeysFormat = "%s is not in stock!";
        GameEntity game = games.get(0);
        String gameId = game.getId();
        games.remove(game);
        game.setKeys(0);

        when(gameService.getGame(gameId)).thenReturn(game);
        when(cartRepository.getGamesByCustomer(customer)).thenReturn(games);
        when(libraryRepository.getGamesByCustomer(customer)).thenReturn(games);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> cartService.addToCart(gameId)
        );
        assertNotNull(exception);
        assertEquals(String.format(notEnoughKeysFormat, game.getTitle()), exception.getMessage());
    }

    @Test
    void testAddToCart_InvalidGameId() {
        mockCustomerAuth();
        GameEntity game = buildGame();
        String gameId = game.getId();
        String noGameFoundFormat = "No game found at id %s!";
        when(gameService.getGame(gameId))
                .thenThrow(new NotFoundException(noGameFoundFormat, gameId));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> cartService.addToCart(gameId)
        );
        assertNotNull(exception);
        assertEquals(String.format(noGameFoundFormat, gameId), exception.getMessage());
    }

    @Test
    void testAddToCart_GameAlreadyInCart() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(3);
        GameEntity game = games.get(0);
        String gameId = game.getId();

        when(gameService.getGame(gameId)).thenReturn(game);
        when(cartRepository.getGamesByCustomer(customer)).thenReturn(games);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> cartService.addToCart(gameId)
        );
        assertNotNull(exception);
        assertEquals(String.format("%s already in cart", game.getTitle()), exception.getMessage());
    }

    @Test
    void testAddToCart_GameAlreadyInLibrary() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(4);
        GameEntity game = games.get(0);
        String gameId = game.getId();

        List<GameEntity> gamesInCart = new ArrayList<>(games);
        gamesInCart.remove(game);
        gamesInCart.removeLast();
        List<GameEntity> gamesInLibrary = new ArrayList<>(games);
        gamesInLibrary.removeAll(gamesInCart);

        when(gameService.getGame(gameId)).thenReturn(game);
        when(cartRepository.getGamesByCustomer(customer)).thenReturn(gamesInCart);
        when(libraryRepository.getGamesByCustomer(customer)).thenReturn(gamesInLibrary);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> cartService.addToCart(gameId)
        );
        assertNotNull(exception);
        assertEquals(String.format("%s already in library", game.getTitle()), exception.getMessage());
    }

    @Test
    void testRemoveFromCart_Valid() {
        UserEntity customer = mockCustomerAuth();
        GameEntity game = buildGame();
        String gameId = game.getId();
        when(gameService.getGame(gameId)).thenReturn(game);

        cartService.removeFromCart(gameId);
        verify(cartRepository, times(1)).delete(buildCartEntry(game,customer));
    }

    @Test
    void testRemoveFromCart_InvalidGameId() {
        mockCustomerAuth();
        GameEntity game = buildGame();
        String gameId = game.getId();
        String noGameFoundFormat = "No game found at id %s!";
        when(gameService.getGame(gameId))
                .thenThrow(new NotFoundException(noGameFoundFormat, gameId));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> cartService.removeFromCart(gameId)
        );
        assertNotNull(exception);
        assertEquals(String.format(noGameFoundFormat, gameId), exception.getMessage());
    }

    @Test
    void testRemoveAllFromCart() {
        UserEntity customer = mockCustomerAuth();
        String customerId = customer.getId();
        doNothing().when(cartRepository).deleteById_CustomerId(customerId);

        cartService.removeAllFromCart();
        verify(cartRepository, times(1)).deleteById_CustomerId(customerId);
    }
}
