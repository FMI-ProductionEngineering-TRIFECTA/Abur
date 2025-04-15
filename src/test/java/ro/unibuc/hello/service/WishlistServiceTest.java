package ro.unibuc.hello.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ro.unibuc.hello.data.entity.CartEntity;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.entity.WishlistEntity;
import ro.unibuc.hello.data.repository.CartRepository;
import ro.unibuc.hello.data.repository.LibraryRepository;
import ro.unibuc.hello.data.repository.WishlistRepository;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.unibuc.hello.data.entity.CartEntity.buildCartEntry;
import static ro.unibuc.hello.data.entity.WishlistEntity.buildWishlistEntry;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.mockCustomerAuth;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.resetMockedAccessToken;
import static ro.unibuc.hello.utils.GameTestUtils.buildGame;
import static ro.unibuc.hello.utils.GameTestUtils.buildGames;

public class WishlistServiceTest {

    @Mock
    protected LibraryRepository libraryRepository;

    @Mock
    protected CartRepository cartRepository;

    @Mock
    protected WishlistRepository wishlistRepository;

    @Mock
    protected GameService gameService;

    @Mock
    private MeterRegistry metricsRegistry;

    @InjectMocks
    private WishlistService wishlistService;

    private static final String noGameFoundFormat = "No game found at id %s!";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resetMockedAccessToken();
    }

    @Test
    void testGetWishlist() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(3);
        Counter counterMock = Mockito.mock(Counter.class);

        when(metricsRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counterMock);
        doNothing().when(counterMock).increment();
        when(wishlistRepository.getGamesByCustomer(customer)).thenReturn(games);

        List<GameEntity> response = wishlistService.getWishlist();
        assertNotNull(response);
        assertEquals(response.size(), games.size());
        assertEquals(response.get(0).getId(), games.get(0).getId());
        assertEquals(response.get(1).getId(), games.get(1).getId());
        assertEquals(response.get(2).getId(), games.get(2).getId());
    }

    @Test
    void testAddToWishlist_Valid() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(3);
        GameEntity game = games.get(0);
        games.remove(game);
        WishlistEntity wishlistEntry = buildWishlistEntry(game, customer);

        when(gameService.getGame(game.getId())).thenReturn(game);
        when(wishlistRepository.getGamesByCustomer(customer)).thenReturn(games);
        when(libraryRepository.getGamesByCustomer(customer)).thenReturn(games);
        when(wishlistRepository.save(wishlistEntry)).thenReturn(wishlistEntry);

        WishlistEntity response = wishlistService.addToWishlist(game.getId());
        assertNotNull(response);
        assertEquals(response, wishlistEntry);
        verify(wishlistRepository, times(1)).save(wishlistEntry);
    }

    @Test
    void testAddToWishlist_InvalidGameId() {
        mockCustomerAuth();
        GameEntity game = buildGame();
        String gameId = game.getId();
        when(gameService.getGame(gameId))
                .thenThrow(new NotFoundException(noGameFoundFormat, gameId));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> wishlistService.addToWishlist(gameId)
        );
        assertNotNull(exception);
        assertEquals(String.format(noGameFoundFormat, gameId), exception.getMessage());
    }

    @Test
    void testAddToWishlist_GameAlreadyInWishlist() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(3);
        GameEntity game = games.get(0);
        String gameId = game.getId();

        when(gameService.getGame(gameId)).thenReturn(game);
        when(wishlistRepository.getGamesByCustomer(customer)).thenReturn(games);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> wishlistService.addToWishlist(gameId)
        );
        assertNotNull(exception);
        assertEquals(String.format("%s already in wishlist", game.getTitle()), exception.getMessage());
    }

    @Test
    void testAddToWishlist_GameAlreadyInLibrary() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(4);
        GameEntity game = games.get(0);
        String gameId = game.getId();

        List<GameEntity> gamesInWishlist = new ArrayList<>(games);
        gamesInWishlist.remove(game);
        gamesInWishlist.removeLast();
        List<GameEntity> gamesInLibrary = new ArrayList<>(games);
        gamesInLibrary.removeAll(gamesInWishlist);

        when(gameService.getGame(gameId)).thenReturn(game);
        when(wishlistRepository.getGamesByCustomer(customer)).thenReturn(gamesInWishlist);
        when(libraryRepository.getGamesByCustomer(customer)).thenReturn(gamesInLibrary);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> wishlistService.addToWishlist(gameId)
        );
        assertNotNull(exception);
        assertEquals(String.format("%s already in library", game.getTitle()), exception.getMessage());
    }

    @Test
    void testMoveToCart_Valid() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(4);
        GameEntity game = games.get(0);
        games.remove(game);
        CartEntity cartEntry = buildCartEntry(game, customer);

        List<GameEntity> gamesInWishlist = new ArrayList<>(List.of(game));
        gamesInWishlist.add(games.getLast());

        when(gameService.getGame(game.getId())).thenReturn(game);
        when(wishlistRepository.getGamesByCustomer(customer)).thenReturn(gamesInWishlist);
        when(cartRepository.getGamesByCustomer(customer)).thenReturn(games);
        when(cartRepository.save(cartEntry)).thenReturn(cartEntry);

        wishlistService.moveToCart(game.getId());
        verify(cartRepository, times(1)).save(cartEntry);
    }

    @Test
    void testMoveToCart_InvalidGameId() {
        mockCustomerAuth();
        GameEntity game = buildGame();
        String gameId = game.getId();
        when(gameService.getGame(gameId))
                .thenThrow(new NotFoundException(noGameFoundFormat, gameId));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> wishlistService.moveToCart(gameId)
        );
        assertNotNull(exception);
        assertEquals(String.format(noGameFoundFormat, gameId), exception.getMessage());
    }

    @Test
    void testMoveToCart_GameNotInWishlist() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(4);
        GameEntity game = games.get(0);
        games.remove(game);
        String gameId = game.getId();
        CartEntity cartEntry = buildCartEntry(game, customer);

        when(gameService.getGame(gameId)).thenReturn(game);
        when(wishlistRepository.getGamesByCustomer(customer)).thenReturn(games);
        when(cartRepository.getGamesByCustomer(customer)).thenReturn(games);
        when(cartRepository.save(cartEntry)).thenReturn(cartEntry);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> wishlistService.moveToCart(gameId)
        );
        assertNotNull(exception);
        assertEquals(String.format("%s is not in wishlist", game.getTitle()), exception.getMessage());
    }

    @Test
    void testMoveToCart_GameAlreadyInCart() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(4);
        GameEntity game = games.get(0);
        String gameId = game.getId();
        CartEntity cartEntry = buildCartEntry(game, customer);

        List<GameEntity> gamesInWishlist = new ArrayList<>(List.of(game));
        gamesInWishlist.add(games.getLast());
        List<GameEntity> gamesInCart = new ArrayList<>(games);

        when(gameService.getGame(gameId)).thenReturn(game);
        when(wishlistRepository.getGamesByCustomer(customer)).thenReturn(gamesInWishlist);
        when(cartRepository.getGamesByCustomer(customer)).thenReturn(gamesInCart);
        when(cartRepository.save(cartEntry)).thenReturn(cartEntry);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> wishlistService.moveToCart(gameId)
        );
        assertNotNull(exception);
        assertEquals(String.format("%s already in cart", game.getTitle()), exception.getMessage());

    }

    @Test
    void testMoveAllToCart_Valid() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(3);
        when(wishlistRepository.getGamesByCustomer(customer)).thenReturn(games);

        ArgumentCaptor<CartEntity> cartCaptor = ArgumentCaptor.forClass(CartEntity.class);
        when(cartRepository.save(any(CartEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        wishlistService.moveAllToCart();

        // capture the arguments and then get the values
        verify(cartRepository, times(games.size())).save(cartCaptor.capture());
        List<CartEntity> capturedCartEntities = cartCaptor.getAllValues();
        assertEquals(games.size(), capturedCartEntities.size());

        for (int i = 0; i< games.size(); i++) {
            GameEntity game = games.get(i);
            assertNotNull(capturedCartEntities.get(i));
            assertEquals(game.getId(), capturedCartEntities.get(i).getGame().getId());
        }
    }

    @Test
    void testMoveAllToCart_NotEnoughKeys() {
        UserEntity customer = mockCustomerAuth();
        List<GameEntity> games = buildGames(4);
        games.get(0).setKeys(0);
        List<GameEntity> gamesOutOfStock = new ArrayList<>(List.of(games.get(0)));
        List<GameEntity> gamesInStock = new ArrayList<>(games);
        gamesInStock.removeAll(gamesOutOfStock);
        when(wishlistRepository.getGamesByCustomer(customer)).thenReturn(games);

        ArgumentCaptor<CartEntity> cartCaptor = ArgumentCaptor.forClass(CartEntity.class);
        when(cartRepository.save(any(CartEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        wishlistService.moveAllToCart();

        // capture the arguments and then get the values
        verify(cartRepository, times(gamesInStock.size())).save(cartCaptor.capture());
        List<CartEntity> capturedCartEntities = cartCaptor.getAllValues();
        assertEquals(gamesInStock.size(), capturedCartEntities.size());

        for (int i = 0; i < gamesInStock.size(); i++) {
            GameEntity game = gamesInStock.get(i);
            assertNotNull(capturedCartEntities.get(i));
            assertEquals(game.getId(), capturedCartEntities.get(i).getGame().getId());
        }
    }

    @Test
    void testRemoveFromWishlist_Valid() {
        UserEntity customer = mockCustomerAuth();
        GameEntity game = buildGame();
        String gameId = game.getId();
        when(gameService.getGame(gameId)).thenReturn(game);

        wishlistService.removeFromWishlist(gameId);
        verify(wishlistRepository, times(1)).delete(buildWishlistEntry(game,customer));
    }

    @Test
    void testRemoveFromWishlist_InvalidGameId() {
        mockCustomerAuth();
        GameEntity game = buildGame();
        String gameId = game.getId();
        when(gameService.getGame(gameId))
                .thenThrow(new NotFoundException(noGameFoundFormat, gameId));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> wishlistService.removeFromWishlist(gameId)
        );
        assertNotNull(exception);
        assertEquals(String.format(noGameFoundFormat, gameId), exception.getMessage());
    }

    @Test
    void testRemoveAllFromWishlist() {
        UserEntity customer = mockCustomerAuth();
        String customerId = customer.getId();
        doNothing().when(cartRepository).deleteById_CustomerId(customerId);

        wishlistService.removeAllFromWishlist();
        verify(wishlistRepository, times(1)).deleteById_CustomerId(customerId);
    }
}
