package ro.unibuc.hello.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.entity.WishlistEntity;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.service.WishlistService;
import ro.unibuc.hello.utils.GenericControllerTest;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.data.entity.WishlistEntity.buildWishlistEntry;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.getMockedAccessToken;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.mockCustomerAuth;
import static ro.unibuc.hello.utils.GameTestUtils.buildGame;
import static ro.unibuc.hello.utils.GameTestUtils.buildGames;

@EnableAspectJAutoProxy
class WishlistControllerTest extends GenericControllerTest<WishlistController> {

    @Mock
    private WishlistService wishlistService;

    @InjectMocks
    private WishlistController wishlistController;

    @Override
    public String getEndpoint() {
        return "wishlist";
    }

    @Override
    public WishlistController getController() {
        return wishlistController;
    }

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        super.setUp();
    }

    @Test
    void testGetWishlist_Valid() throws Exception {
        List<GameEntity> games = buildGames(3);
        when(wishlistService.getWishlist()).thenReturn(games);

        performGet(getMockedAccessToken(Role.CUSTOMER), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(matchAll(games, GAME_FIELDS));
    }

    @Test
    void testGetWishlist_InvalidRole() throws Exception {
        performGet(getMockedAccessToken(Role.DEVELOPER), "")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetWishlist_NoAuth() throws Exception {
        performGet()
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddToWishlist_Valid() throws Exception {
        UserEntity customer = mockCustomerAuth();
        GameEntity game = buildGame();
        WishlistEntity wishlistEntry = buildWishlistEntry(
                game,
                customer
        );
        when(wishlistService.addToWishlist(game.getId())).thenReturn(wishlistEntry);

        performPost(null, getMockedAccessToken(Role.CUSTOMER), "/{gameId}", game.getId())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id.gameId").value(game.getId()))
                .andExpect(jsonPath("$.id.customerId").value(customer.getId()));
    }

    @Test
    void testAddToWishlist_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        when(wishlistService.addToWishlist(any())).thenThrow(new NotFoundException(errorMessage));

        performPost(null, getMockedAccessToken(Role.CUSTOMER), "/{gameId}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testAddToWishlist_InvalidRole() throws Exception {
        performPost(null, getMockedAccessToken(Role.DEVELOPER), "/{gameid}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddToWishlist_NoAuth() throws Exception {
        performPost(null, null, "/{gameid}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMoveToCart_Valid() throws Exception {
        doNothing().when(wishlistService).moveToCart(ID);

        performPost(null, getMockedAccessToken(Role.CUSTOMER), "/moveToCart/{gameId}", ID)
                .andExpect(status().isNoContent());
    }

    @Test
    void testMoveToCart_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        doThrow(new NotFoundException(errorMessage)).when(wishlistService).moveToCart(any());

        performPost(null, getMockedAccessToken(Role.CUSTOMER), "/moveToCart/{gameId}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testMoveToCart_InvalidRole() throws Exception {
        performPost(null, getMockedAccessToken(Role.DEVELOPER), "/moveToCart/{gameId}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMoveToCart_NoAuth() throws Exception {
        performPost(null, null, "/moveToCart/{gameId}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMoveAllToCart_Valid() throws Exception {
        doNothing().when(wishlistService).moveAllToCart();

        performPost(null, getMockedAccessToken(Role.CUSTOMER), "/moveToCart")
                .andExpect(status().isNoContent());
    }

    @Test
    void testMoveAllToCart_InvalidRole() throws Exception {
        performPost(null, getMockedAccessToken(Role.DEVELOPER), "/moveToCart")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMoveAllToCart_NoAuth() throws Exception {
        performPost(null, null, "/moveToCart")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRemoveFromWishlist_Valid() throws Exception {
        performDelete(getMockedAccessToken(Role.CUSTOMER), "/{gameId}", ID)
                .andExpect(status().isNoContent());
    }

    @Test
    void testRemoveFromWishlist_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        doThrow(new NotFoundException(errorMessage)).when(wishlistService).removeFromWishlist(any());

        performDelete(getMockedAccessToken(Role.CUSTOMER), "/{gameId}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testRemoveFromWishlist_InvalidRole() throws Exception {
        performDelete(getMockedAccessToken(Role.DEVELOPER), "/{gameId}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRemoveFromWishlist_NoAuth() throws Exception {
        performDelete(null, "/{gameId}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRemoveAllFromWishlist_Valid() throws Exception {
        performDelete(getMockedAccessToken(Role.CUSTOMER), "/clear")
                .andExpect(status().isNoContent());
    }

    @Test
    void testRemoveAllFromWishlist_InvalidRole() throws Exception {
        performDelete(getMockedAccessToken(Role.DEVELOPER), "/clear")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRemoveAllFromWishlist_NoAuth() throws Exception {
        performDelete(null, "/clear")
                .andExpect(status().isUnauthorized());
    }
}