package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContext;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.*;
import ro.unibuc.hello.exception.NotFoundException;

import java.util.Optional;

import static ro.unibuc.hello.data.entity.GameEntity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.*;

class GameServiceTest {

    @Mock
    protected GameRepository gameRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private GameService gameService = new GameService();

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
        GameEntity game = GameEntity
                .builder()
                .id("1")
                .title("I am a game")
                .build();
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));

        GameEntity result = gameService.getGame(game.getId());

        assertNotNull(result);
        assertEquals(result, game);
    }

    @Test
    void testGetGame_NonExistingGame() {
        String gameId = "game123";
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> gameService.getGame(gameId));
    }

    @Test
    void getGameById() {
        // TODO
    }

    @Test
    void getAllGames() {
        // TODO
    }

    @Test
    void getGameDLCs() {
        // TODO
    }

    @Test
    void createGame() {
        UserEntity developer = mockDeveloperAuth();
        // TODO
        assertNotNull(developer);
    }

    @Test
    void updateGame() {
        // TODO
    }

    @Test
    void addKeys() {
        // TODO
    }

    @Test
    void markOutOfStock() {
        // TODO
    }

    @Test
    void deleteGame() {
        // TODO
    }
}
