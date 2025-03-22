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
import static ro.unibuc.hello.utils.GameTestUtils.*;

@EnableAspectJAutoProxy
public class DeveloperControllerTest extends GenericControllerTest<DeveloperController> {

    @Mock
    private DeveloperService developerService;

    @InjectMocks
    private DeveloperController developerController;

    @Override
    protected String getEndpoint() {
        return "developers";
    }

    @Override
    protected DeveloperController getController() {
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
                .andExpect(jsonPath("$.id").value(mockDeveloper.getId()))
                .andExpect(jsonPath("$.username").value(mockDeveloper.getUsername()))
                .andExpect(jsonPath("$.password").value(mockDeveloper.getPassword()))
                .andExpect(jsonPath("$.email").value(mockDeveloper.getEmail()))
                .andExpect(jsonPath("$.details.studio").value(mockDeveloper.getDetails().getStudio()))
                .andExpect(jsonPath("$.details.website").value(mockDeveloper.getDetails().getWebsite()))
                .andExpect(jsonPath("$.details.firstName").doesNotExist())
                .andExpect(jsonPath("$.details.lastName").doesNotExist());
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
                    .andExpect(jsonPath("$[0].id").value(mockGame.getId()))
                    .andExpect(jsonPath("$[0].title").value(mockGame.getTitle()))
                    .andExpect(jsonPath("$[0].price").value(mockGame.getPrice()))
                    .andExpect(jsonPath("$[0].discountPercentage").value(mockGame.getDiscountPercentage()))
                    .andExpect(jsonPath("$[0].keys").value(mockGame.getKeys()))
                    .andExpect(jsonPath("$[0].type").value(mockGame.getType().toString()))

                    .andExpect(jsonPath("$[1].id").value(mockDLC.getId()))
                    .andExpect(jsonPath("$[1].title").value(mockDLC.getTitle()))
                    .andExpect(jsonPath("$[1].price").value(mockDLC.getPrice()))
                    .andExpect(jsonPath("$[1].discountPercentage").value(mockDLC.getDiscountPercentage()))
                    .andExpect(jsonPath("$[1].keys").value(mockDLC.getKeys()))
                    .andExpect(jsonPath("$[1].type").value(mockDLC.getType().toString()));
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
        when(developerService.getAllUsers()).thenReturn(List.of(mockDeveloper));

        performGet(null, "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(mockDeveloper.getId()))
                    .andExpect(jsonPath("$[0].username").value(mockDeveloper.getUsername()))
                    .andExpect(jsonPath("$[0].password").value(mockDeveloper.getPassword()))
                    .andExpect(jsonPath("$[0].email").value(mockDeveloper.getEmail()))
                    .andExpect(jsonPath("$[0].details.studio").value(mockDeveloper.getDetails().getStudio()))
                    .andExpect(jsonPath("$[0].details.website").value(mockDeveloper.getDetails().getWebsite()))
                    .andExpect(jsonPath("$[0].details.firstName").doesNotExist())
                    .andExpect(jsonPath("$[0].details.lastName").doesNotExist());
    }

    @Test
    void testGetMyGames_Authenticated() throws Exception {
        GameEntity mockGame = buildGame();
        GameEntity mockDLC = buildDLCForGame(mockGame);
        when(developerService.getGames()).thenReturn(List.of(mockGame, mockDLC));

        performGet(getAccessToken(UserEntity.Role.DEVELOPER), "/myGames")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(mockGame.getId()))
                    .andExpect(jsonPath("$[0].title").value(mockGame.getTitle()))
                    .andExpect(jsonPath("$[0].price").value(mockGame.getPrice()))
                    .andExpect(jsonPath("$[0].discountPercentage").value(mockGame.getDiscountPercentage()))
                    .andExpect(jsonPath("$[0].keys").value(mockGame.getKeys()))
                    .andExpect(jsonPath("$[0].type").value(mockGame.getType().toString()))

                    .andExpect(jsonPath("$[1].id").value(mockDLC.getId()))
                    .andExpect(jsonPath("$[1].title").value(mockDLC.getTitle()))
                    .andExpect(jsonPath("$[1].price").value(mockDLC.getPrice()))
                    .andExpect(jsonPath("$[1].discountPercentage").value(mockDLC.getDiscountPercentage()))
                    .andExpect(jsonPath("$[1].keys").value(mockDLC.getKeys()))
                    .andExpect(jsonPath("$[1].type").value(mockDLC.getType().toString()));
    }

    @Test
    void testGetMyGames_Unauthenticated() throws Exception {
        performGet("/myGames")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetMyGames_AuthenticatedCustomer() throws Exception {
        performGet(getAccessToken(UserEntity.Role.CUSTOMER), "/myGames")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateLoggedDeveloper_Authenticated() throws Exception {
        Developer developerInput = mockUpdatedDeveloperInput();
        UserEntity mockUpdatedDeveloper = mockUpdatedDeveloperAuth();
        when(developerService.updateLoggedUser(argThat(devInput -> devInput.equals(developerInput))))
                .thenReturn(mockUpdatedDeveloper);

        performPut(developerInput, getAccessToken(UserEntity.Role.DEVELOPER), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockUpdatedDeveloper.getId()))
                .andExpect(jsonPath("$.username").value(developerInput.getUsername()))
                .andExpect(jsonPath("$.password").value(mockUpdatedDeveloper.getPassword()))
                .andExpect(jsonPath("$.email").value(developerInput.getEmail()))
                .andExpect(jsonPath("$.details.studio").value(developerInput.getStudio()))
                .andExpect(jsonPath("$.details.website").value(developerInput.getWebsite()))
                .andExpect(jsonPath("$.details.firstName").doesNotExist())
                .andExpect(jsonPath("$.details.lastName").doesNotExist());
    }

    @Test
    void testUpdateLoggedDeveloper_AuthenticatedCustomer() throws Exception {
        performPut(mockDeveloperInput(), getAccessToken(UserEntity.Role.CUSTOMER), "")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateLoggedDeveloper_Unauthenticated() throws Exception {
        performPut(mockDeveloperInput(), null, "")
                .andExpect(status().isUnauthorized());
    }

}
