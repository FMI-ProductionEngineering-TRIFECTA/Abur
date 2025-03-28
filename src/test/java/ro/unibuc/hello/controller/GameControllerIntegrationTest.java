package ro.unibuc.hello.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.utils.GenericControllerIntegrationTest;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.utils.GameTestUtils.buildDLCForGame;
import static ro.unibuc.hello.utils.GameTestUtils.buildGame;

public class GameControllerIntegrationTest extends GenericControllerIntegrationTest<GameController>  {

    @Container
    private final static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.20")
            .withExposedPorts(27017)
            .withSharding();

    @DynamicPropertySource
    private static void setProperties(DynamicPropertyRegistry registry) {
        final String MONGO_URL = "mongodb://localhost:";
        final String PORT = String.valueOf(mongoDBContainer.getMappedPort(27017));

        registry.add("mongodb.connection.url", () -> MONGO_URL + PORT);
    }

    private final Integer keysToAdd = 10;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameController gameController;

    @Override
    public String getEndpoint() {
        return "games";
    }

    @Override
    public GameController getController() {
        return gameController;
    }

    @Test
    void testGetGameById_Valid() throws Exception {
        GameEntity game = gameRepository.findByIdAndType(getGameId(0), GameEntity.Type.GAME);

        performGet(null, "/{id}", game.getId())
                .andExpect(status().isOk())
                .andExpect(matchOne(game, GAME_FIELDS));
    }

    @Test
    void testGetGameById_InvalidId() throws Exception {
        performGet(null, "/{id}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No game found at id Invalid ID!"));
    }

    @Test
    void testGetGameDLCs() throws Exception {
        GameEntity game = gameRepository.findByIdAndType(getGameId(0), GameEntity.Type.GAME);
        List<GameEntity> dlcs = game.getDlcs();

        performGet(null, "/{id}/dlcs", game.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(dlcs.size())))
                .andExpect(matchAll(dlcs, GAME_FIELDS));
    }

    @Test
    void testGetGames() throws Exception {
        List<GameEntity> games = gameRepository.findByType(GameEntity.Type.GAME);

        performGet()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(games.size())))
                .andExpect(matchAll(games, GAME_FIELDS));
    }

    @Test
    void testCreateGame_Valid() throws Exception {
        GameEntity game = buildGame();
        Game gameInput = Game.builder().title(game.getTitle()).build();

        performPost(gameInput, getAccessToken(UserEntity.Role.DEVELOPER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(game.getTitle()));
    }

    @Test
    void testCreateGame_InvalidBody() throws Exception {
        performPost(new Game(), getAccessToken(UserEntity.Role.DEVELOPER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Title is required"));
    }

    @Test
    void testCreateGame_InvalidRole() throws Exception {
        performPost(new Game(), getAccessToken(UserEntity.Role.CUSTOMER))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateGame_NoAuth() throws Exception {
        performPost(new Game(), null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddDLC_Valid() throws Exception {
        GameEntity baseGame = gameRepository.findByIdAndType(getGameId(0), GameEntity.Type.GAME);
        GameEntity dlc = buildDLCForGame(baseGame);
        Game dlcInput = Game.builder().title(dlc.getTitle()).build();

        performPost(dlcInput, getAccessToken(UserEntity.Role.DEVELOPER), "/{id}/addDLC", baseGame.getId())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(dlc.getTitle()));
    }

    @Test
    void testAddDLC_InvalidBody() throws Exception {
        performPost(new Game(), getAccessToken(UserEntity.Role.DEVELOPER), "/{id}/addDLC", getGameId(0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Title is required"));
    }

    @Test
    void testAddDLC_InvalidId() throws Exception {
        performPost(new Game(), getAccessToken(UserEntity.Role.DEVELOPER), "/{id}/addDLC", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No game found at id Invalid ID!"));
    }

    @Test
    void testAddDLC_InvalidRole() throws Exception {
        performPost(new Game(), getAccessToken(UserEntity.Role.CUSTOMER), "/{id}/addDLC", getGameId(0))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddDLC_NoAuth() throws Exception {
        performPost(new Game(), null, "/{id}/addDLC", getGameId(0))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateGame_Valid() throws Exception {
        GameEntity game = gameRepository.findByIdAndType(getGameId(0), GameEntity.Type.GAME);
        game.setTitle(game.getTitle() + " Update");

        performPut(Game.builder().title(game.getTitle()).build(), getAccessToken(UserEntity.Role.DEVELOPER), "/{id}", game.getId())
                .andExpect(status().isOk())
                .andExpect(matchOne(game, GAME_FIELDS));
    }

    @Test
    void testUpdateGame_InvalidBody() throws Exception {
        GameEntity game = gameRepository.findByIdAndType(getGameId(0), GameEntity.Type.GAME);
        performPut(Game.builder().title(game.getTitle()).build(), getAccessToken(UserEntity.Role.DEVELOPER), "/{id}", game.getId())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Title is required"));
    }

    @Test
    void testUpdateGame_InvalidId() throws Exception {
        performPut(new Game(), getAccessToken(UserEntity.Role.DEVELOPER), "/{id}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No game found at id Invalid ID!"));
    }

    @Test
    void testUpdateGame_InvalidRole() throws Exception {
        performPut(new Game(), getAccessToken(UserEntity.Role.CUSTOMER), "/{id}", getGameId(0))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateGame_NoAuth() throws Exception {
        performPut(new Game(), null, "/{id}", getGameId(0))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddKeys_Valid() throws Exception {
        GameEntity game = gameRepository.findByIdAndType(getGameId(0), GameEntity.Type.GAME);
        game.setKeys(game.getKeys() + keysToAdd);

        performPut(null, getAccessToken(UserEntity.Role.DEVELOPER), "/{id}/addKeys?quantity={keys}", game.getId(), keysToAdd)
                .andExpect(status().isOk())
                .andExpect(matchOne(game, GAME_FIELDS));
    }

    @Test
    void testAddKeys_InvalidParam() throws Exception {
        performPut(null, getAccessToken(UserEntity.Role.DEVELOPER), "/{id}/addKeys?quantity={keys}", getGameId(0), -keysToAdd)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Number of keys cannot be negative!"));
    }

    @Test
    void testAddKeys_InvalidId() throws Exception {
        performPut(null, getAccessToken(UserEntity.Role.DEVELOPER), "/{id}/addKeys?quantity={keys}", ID, keysToAdd)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No game found at id Invalid ID!"));
    }

    @Test
    void testAddKeys_InvalidRole() throws Exception {
        performPut(null, getAccessToken(UserEntity.Role.CUSTOMER), "/{id}/addKeys?quantity={keys}", getGameId(0), keysToAdd)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddKeys_NoAuth() throws Exception {
        performPut(null, null, "/{id}/addKeys?quantity={keys}", getGameId(0), keysToAdd)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMarkOutOfStock_Valid() throws Exception {
        GameEntity game = gameRepository.findByIdAndType(getGameId(0), GameEntity.Type.GAME);
        game.setKeys(0);

        performPut(null, getAccessToken(UserEntity.Role.DEVELOPER), "/{id}/markOutOfStock", game.getId())
                .andExpect(status().isOk())
                .andExpect(matchOne(game, GAME_FIELDS));
    }

    @Test
    void testMarkOutOfStock_InvalidId() throws Exception {
        performPut(null, getAccessToken(UserEntity.Role.DEVELOPER), "/{id}/markOutOfStock", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value( "No game found at id Invalid ID!"));
    }

    @Test
    void testMarkOutOfStock_InvalidRole() throws Exception {
        performPut(null, getAccessToken(UserEntity.Role.CUSTOMER), "/{id}/markOutOfStock", getGameId(0))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMarkOutOfStock_NoAuth() throws Exception {
        performPut(null, null, "/{id}/markOutOfStock", getGameId(0))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteGame_Valid() throws Exception {
        performDelete(getAccessToken(UserEntity.Role.DEVELOPER), "/{id}", getGameId(0))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteGame_InvalidId() throws Exception {
        performDelete(getAccessToken(UserEntity.Role.DEVELOPER), "/{id}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No game found at id Invalid ID!"));
    }

    @Test
    void testDeleteGame_InvalidRole() throws Exception {
        performDelete(getAccessToken(UserEntity.Role.CUSTOMER), "/{id}", getGameId(0))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteGame_NoAuth() throws Exception {
        performDelete(null, "/{id}", getGameId(0))
                .andExpect(status().isUnauthorized());
    }

}
