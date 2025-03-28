package ro.unibuc.hello.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.service.StoreService;
import ro.unibuc.hello.utils.GenericControllerTest;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.getMockedAccessToken;
import static ro.unibuc.hello.utils.GameTestUtils.buildGames;

@EnableAspectJAutoProxy
class StoreControllerTest extends GenericControllerTest<StoreController> {

    @Mock
    private StoreService storeService;

    @InjectMocks
    private StoreController storeController;

    private static Boolean hideOwned = false;

    @Override
    public String getEndpoint() {
        return "store";
    }

    @Override
    public StoreController getController() {
        return storeController;
    }

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        super.setUp();
    }

    @Test
    void testGetStore_AllGames() throws Exception {
        hideOwned = false;
        List<GameEntity> games = buildGames(3);
        when(storeService.getStore(hideOwned)).thenReturn(games);

        performGet(getMockedAccessToken(Role.CUSTOMER),"?hideOwned={hideOwned}", hideOwned)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].title").value(games.get(0).getTitle()))
                .andExpect(jsonPath("$[1].title").value(games.get(1).getTitle()))
                .andExpect(jsonPath("$[2].title").value(games.get(2).getTitle()));

    }

    @Test
    void testGetStore_HideOwned() throws Exception {
        hideOwned = true;
        List<GameEntity> games = buildGames(3);
        List<GameEntity> owned_games = List.of(games.get(0));
        games.removeAll(owned_games);
        when(storeService.getStore(hideOwned)).thenReturn(games);

        performGet(getMockedAccessToken(Role.CUSTOMER),"?hideOwned={hideOwned}", hideOwned)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value(games.get(0).getTitle()))
                .andExpect(jsonPath("$[1].title").value(games.get(1).getTitle()));
    }

    @Test
    void testGetStore_AllGames_InvalidRole() throws Exception {
        performGet(getMockedAccessToken(Role.DEVELOPER), "?hideOwned={hideOwned}", false)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetStore_HideOwned_InvalidRole() throws Exception {
        performGet(getMockedAccessToken(Role.DEVELOPER), "?hideOwned={hideOwned}", true)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetStore_AllGames_NoAuth() throws Exception {
        performGet(null, "?hideOwned={hideOwned}", false)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetStore_HideOwned_NoAuth() throws Exception {
        performGet(null, "?hideOwned={hideOwned}", true)
                .andExpect(status().isUnauthorized());
    }
}
