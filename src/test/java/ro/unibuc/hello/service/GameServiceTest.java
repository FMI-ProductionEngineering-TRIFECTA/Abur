package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.*;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.exception.UnauthorizedAccessException;
import ro.unibuc.hello.exception.ValidationException;

import java.util.*;

import static ro.unibuc.hello.data.entity.UserEntity.*;
import static ro.unibuc.hello.data.entity.GameEntity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.*;
import static ro.unibuc.hello.utils.GameTestUtils.*;

class GameServiceTest {

    @Mock
    protected GameRepository gameRepository;

    @Mock
    protected UserRepository userRepository;

    @Mock
    @SuppressWarnings("unused")
    protected LibraryRepository libraryRepository;

    @Mock
    @SuppressWarnings("unused")
    protected CartRepository cartRepository;

    @Mock
    @SuppressWarnings("unused")
    protected WishlistRepository wishlistRepository;

    @InjectMocks
    private GameService gameService = new GameService();

    private static final Integer keysToAdd = 10;
    private static final Integer discountPercentage = 50;
    private static final Double price = 59.99;
    private static final String notFoundFormat = "No game found at id %s!";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetType() {
        Type type = gameService.getType();

        assertNotNull(type);
        assertEquals(Type.GAME, type);
    }

    @Test
    void testGetGame_ExistingGame() {
        GameEntity entity = buildGame();
        when(gameRepository.findById(entity.getId())).thenReturn(Optional.of(entity));

        GameEntity game = gameService.getGame(entity.getId());

        assertNotNull(game);
        assertEquals(entity, game);

        verify(gameRepository, times(1)).findById(entity.getId());
    }

    @Test
    void testGetGame_NonExistingGame() {
        String gameId = "invalid-id";
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> gameService.getGame(gameId)
        );
        assertNotNull(exception);
        assertEquals(String.format(notFoundFormat, gameId), exception.getMessage());

        verify(gameRepository, times(1)).findById(gameId);
    }

    @Test
    void getGameById_ExistingGame() {
        GameEntity entity = buildGame();
        when(gameRepository.findById(entity.getId())).thenReturn(Optional.of(entity));

        GameEntity game = gameService.getGameById(entity.getId());

        assertNotNull(game);
        assertEquals(entity, game);

        verify(gameRepository, times(1)).findById(entity.getId());
    }

    @Test
    void getGameById_DLCId() {
        GameEntity entity = buildGame();
        GameEntity dlc = buildDLCsForGame(1, entity).getFirst();
        when(gameRepository.findById(dlc.getId())).thenReturn(Optional.of(dlc));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> gameService.getGameById(dlc.getId())
        );
        assertNotNull(exception);
        assertEquals(String.format("%s is not a game", dlc.getTitle()), exception.getMessage());

        verify(gameRepository, times(1)).findById(dlc.getId());
        verify(gameRepository, times(0)).findById(entity.getId());
    }

    @Test
    void getGameById_NonExistingGame() {
        String gameId = "invalid-id";
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> gameService.getGameById(gameId)
        );
        assertNotNull(exception);
        assertEquals(String.format(notFoundFormat, gameId), exception.getMessage());

        verify(gameRepository, times(1)).findById(gameId);

    }

    @Test
    void testGetAllGames() {
        List<GameEntity> entities = buildGames(2);
        when(gameRepository.findByType(Type.GAME)).thenReturn(entities);

        List<GameEntity> games = gameService.getAllGames();

        assertNotNull(games);
        assertEquals(entities.size(), games.size());
        assertEquals(entities, games);

        verify(gameRepository, times(1)).findByType(Type.GAME);
    }

    @Test
    void testGetGameDLCs_HasDLCs() {
        GameEntity baseGameEntity = buildGame();
        List<GameEntity> dlcEntities = buildDLCsForGame(2, baseGameEntity);
        baseGameEntity.setDlcs(dlcEntities);
        when(gameRepository.findById(baseGameEntity.getId())).thenReturn(Optional.of(baseGameEntity));

        List<GameEntity> dlcs = gameService.getGameDLCs(baseGameEntity.getId());

        assertNotNull(dlcs);
        assertEquals(dlcEntities.size(), dlcs.size());
        assertEquals(dlcEntities, dlcs);

        verify(gameRepository, times(1)).findById(baseGameEntity.getId());
    }

    @Test
    void testGetGameDLCs_NoDLCs() {
        GameEntity baseGameEntity = buildGame();
        when(gameRepository.findById(baseGameEntity.getId())).thenReturn(Optional.of(baseGameEntity));

        List<GameEntity> dlcs = gameService.getGameDLCs(baseGameEntity.getId());

        assertNotNull(dlcs);
        assertEquals(0, dlcs.size());

        verify(gameRepository, times(1)).findById(baseGameEntity.getId());
    }

    @Test
    void testCreateGame_ValidBody() {
        UserEntity developer = mockDeveloperAuth();
        int initialSize = developer.getGames().size();
        GameEntity entity = buildGame(developer);
        when(gameRepository.save(any(GameEntity.class))).thenReturn(entity);

        Game gameInput = Game.builder().title(entity.getTitle()).build();
        GameEntity game = gameService.createGame(gameInput);

        assertNotNull(game);
        assertEquals(entity, game);
        assertEquals(developer, game.getDeveloper());
        assertEquals(developer.getGames().size(), initialSize + 1);
        assert(developer.getGames().contains(game));

        verify(gameRepository, times(1)).save(
                argThat(g -> g.getTitle().equals(entity.getTitle()))
        );
    }

    @Test
    void testCreateGame_InvalidBody() {
        GameEntity entity = buildGame(mockDeveloperAuth());
        when(gameRepository.findByTitle(entity.getTitle())).thenReturn(entity);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> gameService.createGame(Game.builder().title(entity.getTitle()).build())
        );
        assertNotNull(exception);
        assertEquals(String.format("Title %s already exists!", entity.getTitle()), exception.getMessage());

        verify(gameRepository, times(1)).findByTitle(entity.getTitle());
        verify(gameRepository, times(0)).save(any());
    }

    @Test
    void testUpdateGame_ExistingEntityValidBody() {
        UserEntity developer = mockDeveloperAuth();
        GameEntity entity = buildGame(developer);
        when(gameRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        entity.setDiscountPercentage(discountPercentage);
        when(gameRepository.save(any(GameEntity.class))).thenReturn(entity);

        Game gameInput = Game.builder().discountPercentage(discountPercentage).build();
        GameEntity game = gameService.updateGame(entity.getId(), gameInput);

        assertNotNull(game);
        assertEquals(entity, game);
        assertEquals(developer, game.getDeveloper());

        verify(gameRepository, times(1)).findById(entity.getId());
        verify(gameRepository, times(1)).save(
                argThat(g -> g.getTitle().equals(entity.getTitle()))
        );
    }

    @Test
    void testUpdateGame_ExistingEntityInvalidTitle() {
        GameEntity entity = buildGame(mockDeveloperAuth());
        when(gameRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gameRepository.findByTitle(entity.getTitle())).thenReturn(entity);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> gameService.updateGame(entity.getId(), Game.builder().title(entity.getTitle()).build())
        );
        assertNotNull(exception);
        assertEquals(String.format("Title %s already exists!", entity.getTitle()), exception.getMessage());

        verify(gameRepository, times(1)).findById(entity.getId());
        verify(gameRepository, times(0)).save(any());
    }

    @Test
    void testUpdateGame_ExistingEntityInvalidPrice() {
        GameEntity entity = buildGame(mockDeveloperAuth());
        when(gameRepository.findById(entity.getId())).thenReturn(Optional.of(entity));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> gameService.updateGame(entity.getId(), Game.builder().price(-price).build())
        );
        assertNotNull(exception);
        assertEquals("Price cannot be negative!", exception.getMessage());

        verify(gameRepository, times(1)).findById(entity.getId());
        verify(gameRepository, times(0)).save(any());
    }

    @Test
    void testUpdateGame_ExistingEntityInvalidDeveloper() {
        mockDeveloperAuth();
        UserEntity realDeveloper = UserEntity
                .builder()
                .username("Dev")
                .password("Dev123")
                .role(Role.DEVELOPER)
                .build();
        GameEntity entity = buildGame(realDeveloper);
        when(gameRepository.findById(entity.getId())).thenReturn(Optional.of(entity));

        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> gameService.updateGame(entity.getId(), Game.builder().discountPercentage(discountPercentage).build())
        );
        assertNotNull(exception);
        assertNull(exception.getMessage());

        verify(gameRepository, times(1)).findById(entity.getId());
        verify(gameRepository, times(0)).save(any());
    }

    @Test
    void testUpdateGame_NonExistingEntity() {
        GameEntity entity = buildGame(mockDeveloperAuth());
        when(gameRepository.findById(entity.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> gameService.updateGame(entity.getId(), Game.builder().discountPercentage(discountPercentage).build())
        );
        assertNotNull(exception);
        assertEquals(String.format(notFoundFormat, entity.getId()), exception.getMessage());

        verify(gameRepository, times(1)).findById(entity.getId());
        verify(gameRepository, times(0)).save(any());
    }

    @Test
    void testAddKeys_ExistingEntityValidBody() {
        UserEntity developer = mockDeveloperAuth();
        GameEntity entity = buildGame(developer);
        when(gameRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        entity.setKeys(entity.getKeys() + keysToAdd);
        when(gameRepository.save(any(GameEntity.class))).thenReturn(entity);

        GameEntity game = gameService.addKeys(entity.getId(), keysToAdd);

        assertNotNull(game);
        assertEquals(entity, game);
        assertEquals(developer, game.getDeveloper());

        verify(gameRepository, times(1)).findById(entity.getId());
        verify(gameRepository, times(1)).save(
                argThat(g -> g.getTitle().equals(entity.getTitle()))
        );
    }

    @Test
    void testAddKeys_ExistingEntityInvalidKeys() {
        GameEntity entity = buildGame(mockDeveloperAuth());
        when(gameRepository.findById(entity.getId())).thenReturn(Optional.of(entity));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> gameService.addKeys(entity.getId(), -keysToAdd)
        );
        assertNotNull(exception);
        assertEquals("Number of keys cannot be negative!", exception.getMessage());

        verify(gameRepository, times(1)).findById(entity.getId());
        verify(gameRepository, times(0)).save(any());
    }

    @Test
    void testAddKeys_NonExistingEntity() {
        GameEntity entity = buildGame(mockDeveloperAuth());
        when(gameRepository.findById(entity.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> gameService.addKeys(entity.getId(), keysToAdd)
        );
        assertNotNull(exception);
        assertEquals(String.format(notFoundFormat, entity.getId()), exception.getMessage());

        verify(gameRepository, times(1)).findById(entity.getId());
        verify(gameRepository, times(0)).save(any());
    }

    @Test
    void testMarkOutOfStock_ExistingEntity() {
        UserEntity developer = mockDeveloperAuth();
        GameEntity entity = buildGame(developer);
        when(gameRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        entity.setKeys(0);
        when(gameRepository.save(any(GameEntity.class))).thenReturn(entity);

        GameEntity game = gameService.markOutOfStock(entity.getId());

        assertNotNull(game);
        assertEquals(entity, game);
        assertEquals(developer, game.getDeveloper());

        verify(gameRepository, times(1)).findById(entity.getId());
        verify(gameRepository, times(1)).save(
                argThat(g -> g.getTitle().equals(entity.getTitle()))
        );
    }

    @Test
    void testMarkOutOfStock_NonExistingEntity() {
        GameEntity entity = buildGame(mockDeveloperAuth());
        when(gameRepository.findById(entity.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> gameService.markOutOfStock(entity.getId())
        );
        assertNotNull(exception);
        assertEquals(String.format(notFoundFormat, entity.getId()), exception.getMessage());

        verify(gameRepository, times(1)).findById(entity.getId());
        verify(gameRepository, times(0)).save(any());
    }

    @Test
    void testDeleteGame_ExistingEntity() {
        UserEntity developer = mockDeveloperAuth();
        int initialSize = developer.getGames().size();
        GameEntity entity = buildGame(developer);
        when(userRepository.findByIdAndRole(developer.getId(), Role.DEVELOPER)).thenReturn(developer);
        when(gameRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gameRepository.save(any(GameEntity.class))).thenReturn(entity);

        Game gameInput = Game.builder().title(entity.getTitle()).build();
        GameEntity game = gameService.createGame(gameInput);
        assertEquals(initialSize + 1, developer.getGames().size());
        assertTrue(developer.getGames().contains(game));

        gameService.deleteGame(entity.getId());
        assertEquals(initialSize, developer.getGames().size());

        verify(userRepository, times(1)).findByIdAndRole(developer.getId(), Role.DEVELOPER);
        verify(gameRepository, times(1)).findById(entity.getId());
        verify(gameRepository, times(1)).save(
                argThat(g -> g.getTitle().equals(entity.getTitle()))
        );
        verify(gameRepository, times(1)).deleteAll(new ArrayList<>());
        verify(gameRepository, times(1)).delete(entity);
    }

    @Test
    void testDeleteGame_NonExistingEntity() {
        GameEntity entity = buildGame(mockDeveloperAuth());
        when(gameRepository.findById(entity.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> gameService.deleteGame(entity.getId())
        );
        assertNotNull(exception);
        assertEquals(String.format(notFoundFormat, entity.getId()), exception.getMessage());

        verify(gameRepository, times(1)).findById(entity.getId());
        verify(gameRepository, times(0)).deleteAll(any());
        verify(gameRepository, times(0)).delete(any());
    }
}
