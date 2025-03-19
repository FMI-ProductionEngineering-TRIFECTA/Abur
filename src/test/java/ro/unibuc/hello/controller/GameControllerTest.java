package ro.unibuc.hello.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.hello.data.entity.UserEntity;

import static ro.unibuc.hello.utils.AuthenticationTestUtils.*;

class GameControllerTest {

    @InjectMocks
    private GameController gameController;

    private MockMvc mockMvc;

    private String developerToken;
    private String customerToken;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(gameController).build();
        developerToken = getAccessToken(UserEntity.Role.DEVELOPER);
        customerToken = getAccessToken(UserEntity.Role.CUSTOMER);
    }

    @Test
    void getGameById() {
        // TODO
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
    void createGame() {
        // TODO
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