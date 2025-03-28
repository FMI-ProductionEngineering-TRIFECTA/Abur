package ro.unibuc.hello.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.exception.ValidationException;
import ro.unibuc.hello.service.DLCService;
import ro.unibuc.hello.utils.GenericControllerTest;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.getMockedAccessToken;
import static ro.unibuc.hello.utils.DLCTestUtils.*;

@EnableAutoConfiguration
class DLCControllerTest extends GenericControllerTest<DLCController> {

    private final Integer keysToAdd = 10;

    @Mock
    private DLCService dlcService;

    @InjectMocks
    private DLCController dlcController;

    @Override
    public String getEndpoint() {
        return "dlcs";
    }

    @Override
    public DLCController getController() {
        return dlcController;
    }

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        super.setUp();
    }

    @Test
    void testGetDLCyId_Valid() throws Exception {
        GameEntity dlc = buildDLCForGame(buildBaseGame());
        when(dlcService.getGameById(dlc.getId())).thenReturn(dlc);

        performGet(null, "/{id}", dlc.getId())
                .andExpect(status().isOk())
                .andExpect(matchOne(dlc, GAME_FIELDS));
    }

    @Test
    void testGetDLCyId_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        when(dlcService.getGameById(ID)).thenThrow(new NotFoundException(errorMessage));

        performGet(null, "/{id}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testGetDLCs() throws Exception {
        List<GameEntity> dlcs = buildDLCsForGame(3, buildBaseGame());
        when(dlcService.getAllGames()).thenReturn(dlcs);

        performGet()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(matchAll(dlcs, GAME_FIELDS));
    }

    @Test
    void testUpdateDLC_Valid() throws Exception {
        GameEntity dlc = buildDLCForGame(buildBaseGame());
        when(dlcService.updateGame(eq(dlc.getId()), any(Game.class))).thenReturn(dlc);

        performPut(Game.builder().title(dlc.getTitle()).build(), getMockedAccessToken(UserEntity.Role.DEVELOPER), "/{id}", dlc.getId())
                .andExpect(status().isOk())
                .andExpect(matchOne(dlc, GAME_FIELDS));
    }

    @Test
    void testUpdateDLC_InvalidBody() throws Exception {
        String errorMessage = "Invalid body";
        when(dlcService.updateGame(eq(ID),any(Game.class))).thenThrow(new ValidationException(errorMessage));

        performPut(new Game(), getMockedAccessToken(UserEntity.Role.DEVELOPER), "/{id}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testUpdateDLC_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        when(dlcService.updateGame(eq(ID),any(Game.class))).thenThrow(new NotFoundException(errorMessage));

        performPut(new Game(), getMockedAccessToken(UserEntity.Role.DEVELOPER), "/{id}", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testUpdateDLC_InvalidRole() throws Exception {
        performPut(new Game(), getMockedAccessToken(UserEntity.Role.CUSTOMER), "/{id}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateDLC_NoAuth() throws Exception {
        performPut(new Game(), null, "/{id}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddKeys_Valid() throws Exception {
        GameEntity dlc = buildDLCForGame(buildBaseGame());
        dlc.setKeys(dlc.getKeys() + keysToAdd);
        when(dlcService.addKeys(eq(dlc.getId()), any(Integer.class))).thenReturn(dlc);

        performPut(null, getMockedAccessToken(UserEntity.Role.DEVELOPER), "/{id}/addKeys?quantity={keys}", dlc.getId(), keysToAdd)
                .andExpect(status().isOk())
                .andExpect(matchOne(dlc, GAME_FIELDS));
    }

    @Test
    void testAddKeys_InvalidParam() throws Exception {
        String errorMessage = "Invalid parameter";
        when(dlcService.addKeys(eq(ID), any(Integer.class))).thenThrow(new ValidationException(errorMessage));

        performPut(null, getMockedAccessToken(UserEntity.Role.DEVELOPER), "/{id}/addKeys?quantity={keys}", ID, -keysToAdd)
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddKeys_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        when(dlcService.addKeys(eq(ID), any(Integer.class))).thenThrow(new NotFoundException(errorMessage));

        performPut(null, getMockedAccessToken(UserEntity.Role.DEVELOPER), "/{id}/addKeys?quantity={keys}", ID, keysToAdd)
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddKeys_InvalidRole() throws Exception {
        performPut(null, getMockedAccessToken(UserEntity.Role.CUSTOMER), "/{id}/addKeys?quantity={keys}", ID, keysToAdd)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddKeys_NoAuth() throws Exception {
        performPut(null, null, "/{id}/addKeys?quantity={keys}", ID, keysToAdd)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMarkOutOfStock_Valid() throws Exception {
        GameEntity dlc = buildDLCForGame(buildBaseGame());
        dlc.setKeys(0);
        when(dlcService.markOutOfStock(eq(dlc.getId()))).thenReturn(dlc);

        performPut(null, getMockedAccessToken(UserEntity.Role.DEVELOPER), "/{id}/markOutOfStock", dlc.getId())
                .andExpect(status().isOk())
                .andExpect(matchOne(dlc, GAME_FIELDS));
    }

    @Test
    void testMarkOutOfStock_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        when(dlcService.markOutOfStock(eq(ID))).thenThrow(new NotFoundException(errorMessage));

        performPut(null, getMockedAccessToken(UserEntity.Role.DEVELOPER), "/{id}/markOutOfStock", ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testMarkOutOfStock_InvalidRole() throws Exception {
        performPut(null, getMockedAccessToken(UserEntity.Role.CUSTOMER), "/{id}/markOutOfStock", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMarkOutOfStock_NoAuth() throws Exception {
        performPut(null, null, "/{id}/markOutOfStock", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteDLC_Valid() throws Exception {
        performDelete(getMockedAccessToken(UserEntity.Role.DEVELOPER), "/{id}", ID)
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteDLC_InvalidId() throws Exception {
        String errorMessage = "Invalid ID";
        doThrow(new NotFoundException(errorMessage)).when(dlcService).deleteGame(any());

        performDelete(getMockedAccessToken(UserEntity.Role.DEVELOPER), "/{id}", ID)
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteDLC_InvalidRole() throws Exception {
        performDelete(getMockedAccessToken(UserEntity.Role.CUSTOMER), "/{id}", ID)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteDLC_NoAuth() throws Exception {
        performDelete(null, "/{id}", ID)
                .andExpect(status().isUnauthorized());
    }

}