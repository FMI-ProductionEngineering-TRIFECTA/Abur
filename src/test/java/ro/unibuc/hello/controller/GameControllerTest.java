package ro.unibuc.hello.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ro.unibuc.hello.aspect.RoleAuthorizationAspect;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.exception.GlobalExceptionHandler;
import ro.unibuc.hello.service.GameService;

import static org.mockito.Mockito.*;
import static ro.unibuc.hello.data.entity.UserEntity.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.addToken;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.getAccessToken;
import static ro.unibuc.hello.utils.GameTestUtils.buildGame;

@EnableAspectJAutoProxy
class GameControllerTest {

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AspectJProxyFactory factory = new AspectJProxyFactory(gameController);
        factory.addAspect(new RoleAuthorizationAspect());

        mockMvc = MockMvcBuilders
                .standaloneSetup((GameController) factory.getProxy())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ResultActions mockPost(Object requestBody, String token) throws Exception {
        return mockMvc.perform(post("/games")
                .content(new ObjectMapper().writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON)
                .with(addToken(token)));
    }

    @Test
    void getGameById() throws Exception {
        GameEntity entity = buildGame();
        when(gameService.getGameById(entity.getId())).thenReturn(entity);

        mockMvc.perform(get("/games"))
               .andExpect(status().isOk());
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

        mockPost(gameInput, getAccessToken(Role.DEVELOPER))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value(entity.getTitle()));
    }

    @Test
    void testCreateGame_InvalidRole() throws Exception {
        Game gameInput = Game.builder().title(buildGame().getTitle()).build();

        mockPost(gameInput, getAccessToken(Role.CUSTOMER))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateGame_NoAuth() throws Exception {
        Game gameInput = Game.builder().title(buildGame().getTitle()).build();

        mockPost(gameInput, null)
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