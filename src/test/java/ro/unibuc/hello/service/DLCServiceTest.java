package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.exception.UnauthorizedAccessException;
import ro.unibuc.hello.exception.ValidationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.unibuc.hello.data.entity.GameEntity.Type;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.mockDeveloperAuth;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.resetMockedAccessToken;
import static ro.unibuc.hello.utils.DLCTestUtils.buildBaseGame;
import static ro.unibuc.hello.utils.DLCTestUtils.buildDLCForGame;

class DLCServiceTest {

    @Mock
    @SuppressWarnings("unused")
    protected UserRepository userRepository;

    @Mock
    protected GameRepository gameRepository;

    @InjectMocks
    private GameService gameService = new GameService();

    @InjectMocks
    private DLCService dlcService = new DLCService(gameService);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resetMockedAccessToken();
    }

    @Test
    void testGetType() {
        Type type = dlcService.getType();

        assertNotNull(type);
        assertEquals(Type.DLC, type);
    }

    @Test
    void testCreateDLC_Valid() {
        UserEntity developer = mockDeveloperAuth();
        int initialDeveloperSize = developer.getGames().size();
        GameEntity baseGameEntity = buildBaseGame(developer);
        int initialBaseGameSize = baseGameEntity.getDlcs().size();
        GameEntity entity = buildDLCForGame(baseGameEntity);
        entity.setId(null);
        when(gameRepository.findById(baseGameEntity.getId())).thenReturn(Optional.of(baseGameEntity));
        when( gameRepository.save(baseGameEntity)).thenReturn(baseGameEntity);
        when(gameRepository.save(entity)).thenReturn(entity);

        Game dlcInput = Game.builder().title(entity.getTitle()).build();
        GameEntity dlc = dlcService.createDLC(baseGameEntity.getId(), dlcInput);

        assertNotNull(dlc);
        assertEquals(entity, dlc);
        assertEquals(developer, dlc.getDeveloper());
        assertEquals(developer.getGames().size(), initialDeveloperSize + 1);
        assert(baseGameEntity.getDlcs().contains(dlc));
        assertEquals(baseGameEntity.getDlcs().size(), initialBaseGameSize + 1);
        assert(developer.getGames().contains(dlc));

        verify(gameRepository, times(1)).findById(baseGameEntity.getId());
        verify(gameRepository, times(2)).save(any());
    }

    @Test
    void testCreateDLC_InvalidDeveloper() {
        GameEntity baseGameEntity = buildBaseGame();
        GameEntity entity = buildDLCForGame(baseGameEntity);
        entity.setId(null);
        entity.setDeveloper(mockDeveloperAuth());
        baseGameEntity.setDeveloper(UserEntity.builder().username("AnotherDev").password("123").build());
        when(gameRepository.findById(baseGameEntity.getId())).thenReturn(Optional.of(baseGameEntity));

        Game dlcInput = Game.builder().title(entity.getTitle()).baseGame(baseGameEntity).build();
        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> dlcService.createDLC(baseGameEntity.getId(), dlcInput)
        );
        assertNotNull(exception);
        assertNull(exception.getMessage());

        verify(gameRepository, times(1)).findById(baseGameEntity.getId());
        verify(gameRepository, times(0)).save(any());
    }

    @Test
    void testCreateDLC_InvalidBody() {
        GameEntity baseGameEntity = buildBaseGame(mockDeveloperAuth());
        GameEntity entity = buildDLCForGame(baseGameEntity);
        when(gameRepository.findByTitle(entity.getTitle())).thenReturn(entity);
        when(gameRepository.findById(baseGameEntity.getId())).thenReturn(Optional.of(baseGameEntity));

        Game dlcInput = Game.builder().title(entity.getTitle()).baseGame(baseGameEntity).build();
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dlcService.createDLC(baseGameEntity.getId(), dlcInput)
        );
        assertNotNull(exception);
        assertEquals(String.format("Title %s already exists!", entity.getTitle()), exception.getMessage());

        verify(gameRepository, times(1)).findById(baseGameEntity.getId());
        verify(gameRepository, times(1)).findByTitle(entity.getTitle());
        verify(gameRepository, times(0)).save(any());
    }

}
