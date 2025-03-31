package ro.unibuc.hello.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ro.unibuc.hello.data.entity.CartEntity;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.CartInfo;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.service.CartService;
import ro.unibuc.hello.utils.GenericControllerTest;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.data.entity.CartEntity.buildCartEntry;
import static ro.unibuc.hello.data.entity.GameEntity.totalPrice;
import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.getMockedAccessToken;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.mockCustomerAuth;
import static ro.unibuc.hello.utils.GameTestUtils.buildGame;
import static ro.unibuc.hello.utils.GameTestUtils.buildGames;

@EnableAspectJAutoProxy
class CartControllerTest extends GenericControllerTest<CartController> {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @Override
    public String getEndpoint() {
        return "cart";
    }

    @Override
    public CartController getController() {
        return cartController;
    }

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        super.setUp();
    }

    @Test
    void testGetCart_Valid() throws Exception {
        List<GameEntity> games = buildGames(3);
        when(cartService.getCart()).thenReturn(new CartInfo(totalPrice(games),games));

        performGet(getMockedAccessToken(Role.CUSTOMER),"")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(totalPrice(games)))
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(matchAll(".items", games, GAME_FIELDS));
    }

    @Test
    void testGetCart_InvalidRole() throws Exception {
        performGet(getMockedAccessToken(Role.DEVELOPER),"")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetCart_NoAuth() throws Exception {
        performGet()
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCheckout_Valid() throws Exception {
        performPost(null, getMockedAccessToken(Role.CUSTOMER),"/checkout")
                .andExpect(status().isNoContent());
    }

    @Test
    void testCheckout_NotEnoughKeys() throws Exception {
        String errorMessage = "There are no more keys for INSERT_GAME_TITLE, please remove it from the cart!";
        doThrow(new NotFoundException(errorMessage)).when(cartService).checkout();

        performPost(null, getMockedAccessToken(Role.CUSTOMER),"/checkout")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testCheckout_InvalidRole() throws Exception {
        performPost(null, getMockedAccessToken(Role.DEVELOPER),"/checkout")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCheckout_NoAuth() throws Exception {
        performPost(null, null,"/checkout")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddToCart_Valid() throws Exception {
        UserEntity customer = mockCustomerAuth();
        GameEntity game = buildGame();
        CartEntity cart = buildCartEntry(
                game,
                customer
        );
        when(cartService.addToCart(game.getId())).thenReturn(cart);

        performPost(null, getMockedAccessToken(Role.CUSTOMER),"/{gameId}", game.getId())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id.gameId").value(game.getId()))
                .andExpect(jsonPath("$.id.customerId").value(customer.getId()));
    }

    @Test
    void testAddToCart_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        when(cartService.addToCart(any())).thenThrow(new NotFoundException(errorMessage));

        performPost(null, getMockedAccessToken(Role.CUSTOMER),"/{gameId}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testAddToCart_InvalidRole() throws Exception {
        performPost(null, getMockedAccessToken(Role.DEVELOPER),"/{gameid}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddToCart_NoAuth() throws Exception {
        performPost(null, null, "/{gameid}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRemoveFromCart_Valid() throws Exception {
        performDelete(getMockedAccessToken(Role.CUSTOMER), "/{gameId}", ID)
                .andExpect(status().isNoContent());
    }

    @Test
    void testRemoveFromCart_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        doThrow(new NotFoundException(errorMessage)).when(cartService).removeFromCart(any());

        performDelete(getMockedAccessToken(Role.CUSTOMER), "/{gameId}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testRemoveFromCart_InvalidRole() throws Exception {
        performDelete(getMockedAccessToken(Role.DEVELOPER), "/{gameId}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRemoveFromCart_NoAuth() throws Exception {
        performDelete(null, "/{gameId}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testClearCart_Valid() throws Exception {
        performDelete(getMockedAccessToken(Role.CUSTOMER), "/clear")
                .andExpect(status().isNoContent());
    }

    @Test
    void testClearCart_InvalidRole() throws Exception {
        performDelete(getMockedAccessToken(Role.DEVELOPER), "/clear")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testClearCart_NoAuth() throws Exception {
        performDelete(null, "/clear")
                .andExpect(status().isUnauthorized());
    }
}
