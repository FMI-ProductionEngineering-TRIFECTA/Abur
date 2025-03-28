package ro.unibuc.hello.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.CartRepository;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.utils.GenericControllerIntegrationTest;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.data.entity.GameEntity.Type;
import static ro.unibuc.hello.data.entity.GameEntity.totalPrice;
import static ro.unibuc.hello.data.entity.UserEntity.Role;

public class CartControllerIntegrationTest extends GenericControllerIntegrationTest<CartController> {

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

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CartController cartController;

    @Override
    public String getEndpoint() {
        return "cart";
    }

    @Override
    public CartController getController() {
        return cartController;
    }

    private void testPerformGet(List<GameEntity> games) throws Exception {
        performGet(getAccessToken(Role.CUSTOMER), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(totalPrice(games)))
                .andExpect(jsonPath("$.items", hasSize(games.size())))
                .andExpect(matchAll(".items", games, GAME_FIELDS));
    }

    @Test
    void testGetCart_Valid() throws Exception {
        UserEntity user = userRepository.findByIdAndRole(getUserId(Role.CUSTOMER), Role.CUSTOMER);
        List<GameEntity> games = cartRepository.getGamesByCustomer(user);

        testPerformGet(games);
    }

    @Test
    void testGetCart_InvalidRole() throws Exception {
        performGet(getAccessToken(Role.DEVELOPER),"")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetCart_NoAuth() throws Exception {
        performGet()
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCheckout_Valid() throws Exception {
        performPost(null, getAccessToken(Role.CUSTOMER),"/checkout")
                .andExpect(status().isNoContent());
    }

    @Test
    void testCheckout_InvalidRole() throws Exception {
        performPost(null, getAccessToken(Role.DEVELOPER),"/checkout")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCheckout_NoAuth() throws Exception {
        performPost(null, null,"/checkout")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddToCart_Valid() throws Exception {
        UserEntity user = userRepository.findByIdAndRole(getUserId(Role.CUSTOMER), Role.CUSTOMER);
        ArrayList<GameEntity> games = new ArrayList<>(cartRepository.getGamesByCustomer(user));
        GameEntity game = gameRepository.findByIdAndType(getGameId(6), Type.GAME);

        performPost(null, getAccessToken(Role.CUSTOMER),"/{gameId}", game.getId())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id.gameId").value(game.getId()))
                .andExpect(jsonPath("$.id.customerId").value(getUserId(Role.CUSTOMER)));

        games.add(game);
        testPerformGet(games);
    }

    @Test
    void testAddToCart_InvalidId() throws Exception {
        performPost(null, getAccessToken(Role.CUSTOMER),"/{gameId}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(String.format("No game found at id %s!", ID)));
    }

    @Test
    void testAddToCart_InvalidRole() throws Exception {
        performPost(null, getAccessToken(Role.DEVELOPER),"/{gameid}", getGameId(6))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddToCart_NoAuth() throws Exception {
        performPost(null, null, "/{gameid}", getGameId(6))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRemoveFromCart_Valid() throws Exception {
        UserEntity user = userRepository.findByIdAndRole(getUserId(Role.CUSTOMER), Role.CUSTOMER);
        ArrayList<GameEntity> games = new ArrayList<>(cartRepository.getGamesByCustomer(user));
        GameEntity game = gameRepository.findByIdAndType(getGameId(3), Type.GAME);

        performDelete(getAccessToken(Role.CUSTOMER), "/{gameId}", game.getId())
                .andExpect(status().isNoContent());

        games.remove(game);
        testPerformGet(games);
    }

    @Test
    void testRemoveFromCart_InvalidId() throws Exception {
        performDelete(getAccessToken(Role.CUSTOMER), "/{gameId}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(String.format("No game found at id %s!", ID)));
    }

    @Test
    void testRemoveFromCart_InvalidRole() throws Exception {
        performDelete(getAccessToken(Role.DEVELOPER), "/{gameId}", getGameId(3))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRemoveFromCart_NoAuth() throws Exception {
        performDelete(null, "/{gameId}", getGameId(3))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testClearCart_Valid() throws Exception {
        performDelete(getAccessToken(Role.CUSTOMER), "/clear")
                .andExpect(status().isNoContent());

        performGet(getAccessToken(Role.CUSTOMER), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(0))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void testClearCart_InvalidRole() throws Exception {
        performDelete(getAccessToken(Role.DEVELOPER), "/clear")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testClearCart_NoAuth() throws Exception {
        performDelete(null, "/clear")
                .andExpect(status().isUnauthorized());
    }

}