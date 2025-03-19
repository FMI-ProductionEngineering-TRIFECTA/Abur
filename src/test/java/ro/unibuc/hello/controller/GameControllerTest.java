package ro.unibuc.hello.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.exception.ValidationException;
import ro.unibuc.hello.service.GameService;
import ro.unibuc.hello.utils.GenericControllerTest;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.getAccessToken;
import static ro.unibuc.hello.utils.GameTestUtils.buildGame;

@EnableAspectJAutoProxy
class GameControllerTest extends GenericControllerTest<GameController> {

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    @Override
    protected String getEndpoint() {
        return "games";
    }

    @Override
    protected GameController getController() {
        return gameController;
    }

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        super.setUp();
    }

    @Test
    void testGetGameById_ValidId() throws Exception {
        GameEntity entity = buildGame();
        when(gameService.getGameById(entity.getId())).thenReturn(entity);

        performGet("/{id}", entity.getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(entity.getId()))
            .andExpect(jsonPath("$.title").value(entity.getTitle()));
    }

    @Test
    void testGetGameById_InvalidId() throws Exception {
        String gameId = "1";
        when(gameService.getGameById(gameId)).thenThrow(new ValidationException("Invalid id"));

        performGet("/{id}", gameId)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invalid id"));
    }

    @Test
    void getGameDLCs() {
        // TODO
    }

    @Test
    void getGames() {
        // TODO
    }

    @Test
    void testCreateGame_ValidBody() throws Exception {
        GameEntity entity = buildGame();
        Game gameInput = Game.builder().title(entity.getTitle()).build();
        when(gameService.createGame(any(Game.class))).thenReturn(entity);

        performPost(gameInput, getAccessToken(Role.DEVELOPER))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value(entity.getTitle()));
    }

    @Test
    void testCreateGame_InvalidRole() throws Exception {
        performPost(Game.builder().title(buildGame().getTitle()).build(), getAccessToken(Role.CUSTOMER))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateGame_NoAuth() throws Exception {
        performPost(Game.builder().title(buildGame().getTitle()).build(), null)
            .andExpect(status().isUnauthorized());
    }

    @Test
    void addDLC() {
        // TODO
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