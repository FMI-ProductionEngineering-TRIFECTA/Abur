package ro.unibuc.hello.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.service.DeveloperService;
import ro.unibuc.hello.utils.GenericControllerTest;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.*;
import static ro.unibuc.hello.utils.GameTestUtils.buildDLCForGame;
import static ro.unibuc.hello.utils.GameTestUtils.buildGame;

@EnableAspectJAutoProxy
public class DeveloperControllerTest extends GenericControllerTest<DeveloperController> {

    @Mock
    private DeveloperService developerService;

    @InjectMocks
    private DeveloperController developerController;

    @Override
    public String getEndpoint() {
        return "developers";
    }

    @Override
    public DeveloperController getController() {
        return developerController;
    }

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        super.setUp();
    }

    @Test
    void testGetDeveloperById_ValidId() throws Exception {
        UserEntity mockDeveloper = mockDeveloperAuth();
        when(developerService.getDeveloper(mockDeveloper.getId())).thenReturn(mockDeveloper);

        performGet(null,"/{id}", mockDeveloper.getId())
                .andExpect(status().isOk())
                .andExpect(matchOne(mockDeveloper, DEVELOPER_FIELDS));
    }

    @Test
    void testGetDeveloperById_InvalidId() throws Exception {
        final String id = "id-invalid";
        final String errorMessage = String.format("No %s found at id %s", UserEntity.Role.DEVELOPER.toString().toLowerCase(), id);
        when(developerService.getDeveloper(id)).thenThrow(new NotFoundException(errorMessage));

        performGet(null, "/{id}", id)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testGetDeveloperGames_ValidId() throws Exception {
        UserEntity mockDeveloper = mockDeveloperAuth();
        GameEntity mockGame = buildGame();
        GameEntity mockDLC = buildDLCForGame(mockGame);
        when(developerService.getGames(mockDeveloper.getId())).thenReturn(List.of(mockGame, mockDLC));

        performGet(null, "/{id}/games", mockDeveloper.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(matchAll(List.of(mockGame, mockDLC), GAME_FIELDS));
    }

    @Test
    void testGetDeveloperGames_InvalidId() throws Exception {
        final String id = "id-invalid";
        final String errorMessage = String.format("No %s found at id %s", UserEntity.Role.DEVELOPER.toString().toLowerCase(), id);
        when(developerService.getGames(id)).thenThrow(new NotFoundException(errorMessage));

        performGet(null, "/{id}/games", id)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testGetAllDevelopers() throws Exception {
        UserEntity mockDeveloper = mockDeveloperAuth();
        List<UserEntity> mockDevelopers = List.of(mockDeveloper);
        when(developerService.getAllUsers()).thenReturn(mockDevelopers);

        performGet(null, "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(matchAll(mockDevelopers, DEVELOPER_FIELDS));
    }

    @Test
    void testGetMyGames_Authenticated() throws Exception {
        GameEntity mockGame = buildGame();
        GameEntity mockDLC = buildDLCForGame(mockGame);
        when(developerService.getGames()).thenReturn(List.of(mockGame, mockDLC));

        performGet(getMockedAccessToken(UserEntity.Role.DEVELOPER), "/myGames")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(matchAll(List.of(mockGame, mockDLC), GAME_FIELDS));
    }

    @Test
    void testGetMyGames_Unauthenticated() throws Exception {
        performGet("/myGames")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetMyGames_AuthenticatedCustomer() throws Exception {
        performGet(getMockedAccessToken(UserEntity.Role.CUSTOMER), "/myGames")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateLoggedDeveloper_Authenticated() throws Exception {
        Developer developerInput = mockUpdatedDeveloperInput();
        UserEntity mockUpdatedDeveloper = mockUpdatedDeveloperAuth();
        when(developerService.updateLoggedUser(argThat(devInput -> devInput.equals(developerInput))))
                .thenReturn(mockUpdatedDeveloper);

        performPut(developerInput, getMockedAccessToken(UserEntity.Role.DEVELOPER), "")
                .andExpect(status().isOk())
                .andExpect(matchOne(mockUpdatedDeveloper, DEVELOPER_FIELDS));
    }

    @Test
    void testUpdateLoggedDeveloper_AuthenticatedCustomer() throws Exception {
        performPut(mockDeveloperInput(), getMockedAccessToken(UserEntity.Role.CUSTOMER), "")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateLoggedDeveloper_Unauthenticated() throws Exception {
        performPut(mockDeveloperInput(), null, "")
                .andExpect(status().isUnauthorized());
    }

}
