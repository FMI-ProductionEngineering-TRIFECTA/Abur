package ro.unibuc.hello.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.service.LibraryService;
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
class LibraryControllerTest extends GenericControllerTest<LibraryController> {

    @Mock
    private LibraryService libraryService;

    @InjectMocks
    private LibraryController libraryController;

    @Override
    public String getEndpoint() {
        return "library";
    }

    @Override
    public LibraryController getController() {
        return libraryController;
    }

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        super.setUp();
    }

    @Test
    void testGetLibrary_Valid() throws Exception {
        List<GameEntity> games = buildGames(3);
        when(libraryService.getLibrary()).thenReturn(games);

        performGet(getMockedAccessToken(Role.CUSTOMER), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(matchAll(games, GAME_FIELDS));
    }

    @Test
    void testGetLibrary_InvalidRole() throws Exception {
        performGet(getMockedAccessToken(Role.DEVELOPER), "")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetLibrary_NoAuth() throws Exception {
        performGet()
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetLibraryById_Valid() throws Exception {
        List<GameEntity> games = buildGames(3);
        when(libraryService.getLibraryByCustomerId(ID)).thenReturn(games);

        performGet(getMockedAccessToken(Role.CUSTOMER), "/{customerId}", ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(matchAll(games, GAME_FIELDS));
    }

    @Test
    void testGetLibraryById_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        when(libraryService.getLibraryByCustomerId(ID)).thenThrow(new NotFoundException(errorMessage));

        performGet(getMockedAccessToken(Role.CUSTOMER), "/{customerId}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }
}
