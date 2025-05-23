package ro.unibuc.hello.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.exception.ValidationException;
import ro.unibuc.hello.service.DLCService;
import ro.unibuc.hello.service.GameService;
import ro.unibuc.hello.utils.GenericControllerTest;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.getMockedAccessToken;
import static ro.unibuc.hello.utils.GameTestUtils.*;

@EnableAspectJAutoProxy
class GameControllerTest extends GenericControllerTest<GameController> {

    private final Integer keysToAdd = 10;

    @Mock
    private GameService gameService;

    @Mock
    private DLCService dlcService;

    @InjectMocks
    private GameController gameController;

    @Override
    public String getEndpoint() {
        return "games";
    }

    @Override
    public GameController getController() {
        return gameController;
    }

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        super.setUp();
    }

    @Test
    void testGetGameById_Valid() throws Exception {
        GameEntity game = buildGame();
        when(gameService.getGameById(game.getId())).thenReturn(game);

        performGet(null, "/{id}", game.getId())
                .andExpect(status().isOk())
                .andExpect(matchOne(game, GAME_FIELDS));
    }

    @Test
    void testGetGameById_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        when(gameService.getGameById(ID)).thenThrow(new NotFoundException(errorMessage));

        performGet(null, "/{id}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testGetGameDLCs() throws Exception {
        GameEntity game = buildGame();
        List<GameEntity> dlcs = buildDLCsForGame(3, game);
        when(gameService.getGameDLCs(game.getId())).thenReturn(dlcs);

        performGet(null, "/{id}/dlcs", game.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(matchAll(dlcs, GAME_FIELDS));
    }

    @Test
    void testGetGames() throws Exception {
        List<GameEntity> games = buildGames(3);
        when(gameService.getAllGames()).thenReturn(games);

        performGet()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(matchAll(games, GAME_FIELDS));
    }

    @Test
    void testCreateGame_Valid() throws Exception {
        GameEntity game = buildGame();
        Game gameInput = Game.builder().title(game.getTitle()).build();
        when(gameService.createGame(any(Game.class))).thenReturn(game);

        performPost(gameInput, getMockedAccessToken(Role.DEVELOPER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(game.getTitle()));
    }

    @Test
    void testCreateGame_InvalidBody() throws Exception {
        String errorMessage = "Invalid body";
        when(gameService.createGame(any(Game.class))).thenThrow(new ValidationException(errorMessage));

        performPost(new Game(), getMockedAccessToken(Role.DEVELOPER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testCreateGame_InvalidRole() throws Exception {
        performPost(new Game(), getMockedAccessToken(Role.CUSTOMER))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateGame_NoAuth() throws Exception {
        performPost(new Game(), null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddDLC_Valid() throws Exception {
        GameEntity baseGame = buildGame();
        GameEntity dlc = buildDLCForGame(baseGame);
        Game dlcInput = Game.builder().title(dlc.getTitle()).build();
        when(dlcService.createDLC(eq(baseGame.getId()), any(Game.class))).thenReturn(dlc);

        performPost(dlcInput, getMockedAccessToken(Role.DEVELOPER), "/{id}/addDLC", baseGame.getId())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(dlc.getTitle()));
    }

    @Test
    void testAddDLC_InvalidBody() throws Exception {
        String errorMessage = "Invalid body";
        when(dlcService.createDLC(eq(ID),any(Game.class))).thenThrow(new ValidationException(errorMessage));

        performPost(new Game(), getMockedAccessToken(Role.DEVELOPER), "/{id}/addDLC", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testAddDLC_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        when(dlcService.createDLC(eq(ID),any(Game.class))).thenThrow(new NotFoundException(errorMessage));

        performPost(new Game(), getMockedAccessToken(Role.DEVELOPER), "/{id}/addDLC", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testAddDLC_InvalidRole() throws Exception {
        performPost(new Game(), getMockedAccessToken(Role.CUSTOMER), "/{id}/addDLC", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddDLC_NoAuth() throws Exception {
        performPost(new Game(), null, "/{id}/addDLC", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateGame_Valid() throws Exception {
        GameEntity game = buildGame();
        when(gameService.updateGame(eq(game.getId()), any(Game.class))).thenReturn(game);

        performPut(Game.builder().title(game.getTitle() + " Update").build(), getMockedAccessToken(Role.DEVELOPER), "/{id}", game.getId())
                .andExpect(status().isOk())
                .andExpect(matchOne(game, GAME_FIELDS));
    }

    @Test
    void testUpdateGame_InvalidBody() throws Exception {
        String errorMessage = "Invalid body";
        when(gameService.updateGame(eq(ID),any(Game.class))).thenThrow(new ValidationException(errorMessage));

        performPut(new Game(), getMockedAccessToken(Role.DEVELOPER), "/{id}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testUpdateGame_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        when(gameService.updateGame(eq(ID),any(Game.class))).thenThrow(new NotFoundException(errorMessage));

        performPut(new Game(), getMockedAccessToken(Role.DEVELOPER), "/{id}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testUpdateGame_InvalidRole() throws Exception {
        performPut(new Game(), getMockedAccessToken(Role.CUSTOMER), "/{id}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateGame_NoAuth() throws Exception {
        performPut(new Game(), null, "/{id}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddKeys_Valid() throws Exception {
        GameEntity game = buildGame();
        game.setKeys(game.getKeys() + keysToAdd);
        when(gameService.addKeys(eq(game.getId()), any(Integer.class))).thenReturn(game);

        performPut(null, getMockedAccessToken(Role.DEVELOPER), "/{id}/addKeys?quantity={keys}", game.getId(), keysToAdd)
                .andExpect(status().isOk())
                .andExpect(matchOne(game, GAME_FIELDS));
    }

    @Test
    void testAddKeys_InvalidParam() throws Exception {
        String errorMessage = "Invalid parameter";
        when(gameService.addKeys(eq(ID), any(Integer.class))).thenThrow(new ValidationException(errorMessage));

        performPut(null, getMockedAccessToken(Role.DEVELOPER), "/{id}/addKeys?quantity={keys}", ID, -keysToAdd)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testAddKeys_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        when(gameService.addKeys(eq(ID), any(Integer.class))).thenThrow(new NotFoundException(errorMessage));

        performPut(null, getMockedAccessToken(Role.DEVELOPER), "/{id}/addKeys?quantity={keys}", ID, keysToAdd)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testAddKeys_InvalidRole() throws Exception {
        performPut(null, getMockedAccessToken(Role.CUSTOMER), "/{id}/addKeys?quantity={keys}", ID, keysToAdd)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddKeys_NoAuth() throws Exception {
        performPut(null, null, "/{id}/addKeys?quantity={keys}", ID, keysToAdd)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMarkOutOfStock_Valid() throws Exception {
        GameEntity game = buildGame();
        game.setKeys(0);
        when(gameService.markOutOfStock(eq(game.getId()))).thenReturn(game);

        performPut(null, getMockedAccessToken(Role.DEVELOPER), "/{id}/markOutOfStock", game.getId())
                .andExpect(status().isOk())
                .andExpect(matchOne(game, GAME_FIELDS));
    }

    @Test
    void testMarkOutOfStock_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        when(gameService.markOutOfStock(eq(ID))).thenThrow(new NotFoundException(errorMessage));

        performPut(null, getMockedAccessToken(Role.DEVELOPER), "/{id}/markOutOfStock", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testMarkOutOfStock_InvalidRole() throws Exception {
        performPut(null, getMockedAccessToken(Role.CUSTOMER), "/{id}/markOutOfStock", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMarkOutOfStock_NoAuth() throws Exception {
        performPut(null, null, "/{id}/markOutOfStock", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteGame_Valid() throws Exception {
        performDelete(getMockedAccessToken(Role.DEVELOPER), "/{id}", ID)
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteGame_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        doThrow(new NotFoundException(errorMessage)).when(gameService).deleteGame(any());

        performDelete(getMockedAccessToken(Role.DEVELOPER), "/{id}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testDeleteGame_InvalidRole() throws Exception {
        performDelete(getMockedAccessToken(Role.CUSTOMER), "/{id}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteGame_NoAuth() throws Exception {
        performDelete(null, "/{id}", ID)
                .andExpect(status().isUnauthorized());
    }

}
