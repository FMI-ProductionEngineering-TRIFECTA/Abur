package ro.unibuc.hello.controller;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.CartRepository;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.service.CartService;
import ro.unibuc.hello.service.StoreService;
import ro.unibuc.hello.utils.GenericControllerIntegrationTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.data.entity.GameEntity.totalPrice;
import static ro.unibuc.hello.data.entity.UserEntity.Role;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("IntegrationTest")
public class CartControllerIntegrationTest extends GenericControllerIntegrationTest<CartController> {

    @Autowired
    private StoreService storeService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

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

    @Test
    void testGetCart_Valid() throws Exception {
        UserEntity user = userRepository.findByIdAndRole(getUserId(Role.CUSTOMER), Role.CUSTOMER);
        List<GameEntity> games = cartRepository.getGamesByCustomer(user);

        performGet(getAccessToken(Role.CUSTOMER), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(totalPrice(games)))
                .andExpect(jsonPath("$.items", hasSize(games.size())))
                .andExpect(jsonPath("$.items[0].title").value(games.get(0).getTitle()))
                .andExpect(jsonPath("$.items[1].title").value(games.get(1).getTitle()))
                .andExpect(jsonPath("$.items[2].title").value(games.get(2).getTitle()));
    }

}