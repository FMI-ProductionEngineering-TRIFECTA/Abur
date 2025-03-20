package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.exception.ValidationException;
import ro.unibuc.hello.security.AuthenticationUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.*;
import static ro.unibuc.hello.utils.GameTestUtils.buildGames;

public class DeveloperServiceTest {

    @Mock
    protected UserRepository userRepository;

    @InjectMocks
    private DeveloperService developerService = new DeveloperService();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRole() {
        UserEntity.Role role = developerService.getRole();

        assertEquals(UserEntity.Role.DEVELOPER, role);
    }

    @Test
    void testGetDeveloper_ValidId() {
        UserEntity mockDeveloper = mockDeveloperAuth();
        when(userRepository.findByIdAndRole(mockDeveloper.getId(), mockDeveloper.getRole())).thenReturn(mockDeveloper);

        UserEntity developer = developerService.getDeveloper(mockDeveloper.getId());

        assertEquals(mockDeveloper, developer);
    }

    @Test
    void testGetDeveloper_InvalidId() {
        final String id = "id-invalid";
        NotFoundException exception = assertThrows(NotFoundException.class, () -> developerService.getDeveloper(id));
        assertEquals("No developer found at id " + id, exception.getMessage());
    }

    @Test
    void testGetAllDevelopers() {
        List<UserEntity> mockUsers = buildDevelopers(2);
        when(userRepository.findByRole(developerService.getRole())).thenReturn(mockUsers);

        List<UserEntity> users = developerService.getAllUsers();

        assertNotNull(users);
        assertEquals(mockUsers.size(), users.size());
        assertEquals(mockUsers, users);
    }

    @Test
    void testDeveloperGetAllGames_ValidId() {
        UserEntity mockDeveloper = mockDeveloperAuth();
        mockDeveloper.setGames(buildGames(2));
        when(userRepository.findByIdAndRole(mockDeveloper.getId(), mockDeveloper.getRole())).thenReturn(mockDeveloper);

        List<GameEntity> games = developerService.getGames(mockDeveloper.getId());

        assertNotNull(games);
        assertEquals(mockDeveloper.getGames().size(), games.size());
        assertEquals(mockDeveloper.getGames(), games);
    }

    @Test
    void testDeveloperGetAllGames_InvalidId() {
        final String id = "id-invalid";

        NotFoundException exception = assertThrows(NotFoundException.class, () -> developerService.getGames(id));
        assertEquals("No developer found at id " + id, exception.getMessage());
    }

    @Test
    void testDeveloperGetMyGames_Authenticated() {
        UserEntity mockDeveloper = mockDeveloperAuth();
        mockDeveloper.setGames(buildGames(2));

        List<GameEntity> games = developerService.getGames();

        assertNotNull(games);
        assertEquals(mockDeveloper.getGames().size(), games.size());
        assertEquals(mockDeveloper.getGames(), games);
    }

    @Test
    void testDeveloperGetMyGames_Unauthenticated() {
        // SecurityContextHolder.getContext().setAuthentication(null);

        // TODO: UnauthorizedAccessException not thrown
        // assertThrows(UnauthorizedAccessException.class, () -> developerService.getGames());
    }

    @Test
    void testUpdateDeveloper_AuthenticatedValid() {
        Developer developerInput = mockUpdatedDeveloper();
        mockDeveloperAuth();

        developerService.updateLoggedUser(developerInput);

        verify(userRepository, times(1)).save(
                argThat(dev
                        -> dev.getUsername().equals(developerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(developerInput.getPassword(), dev.getPassword())
                        && dev.getEmail().equals(developerInput.getEmail())
                        && dev.getRole().equals(UserEntity.Role.DEVELOPER)
                        && dev.getDetails().getStudio().equals(developerInput.getStudio())
                        && dev.getDetails().getWebsite().equals(developerInput.getWebsite())
                        && dev.getDetails().getFirstName() == null
                        && dev.getDetails().getLastName() == null
                )
        );
    }

    @Test
    void testUpdateDeveloper_Authenticated_NullUsername() {
        Developer developerInput = mockUpdatedDeveloper();
        developerInput.setUsername(null);
        mockDeveloperAuth();

        developerService.updateLoggedUser(developerInput);

        verify(userRepository, times(1)).save(
                argThat(dev
                        -> dev.getUsername().equals(mockDeveloper().getUsername())
                        && AuthenticationUtils.isPasswordValid(developerInput.getPassword(), dev.getPassword())
                        && dev.getEmail().equals(developerInput.getEmail())
                        && dev.getRole().equals(UserEntity.Role.DEVELOPER)
                        && dev.getDetails().getStudio().equals(developerInput.getStudio())
                        && dev.getDetails().getWebsite().equals(developerInput.getWebsite())
                        && dev.getDetails().getFirstName() == null
                        && dev.getDetails().getLastName() == null
                )
        );
    }

    @Test
    void testUpdateDeveloper_Authenticated_NonUniqueUsername() {
        Developer developerInput = mockUpdatedDeveloper();
        developerInput.setUsername(mockDeveloper().getUsername());
        UserEntity developer = mockDeveloperAuth();

        when(userRepository.findByUsername(developer.getUsername())).thenReturn(developer);

        ValidationException exception = assertThrows(ValidationException.class, () -> developerService.updateLoggedUser(developerInput));
        assertEquals("Username developer already exists!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateDeveloper_Authenticated_BlankUsername() {
        Developer developerInput = mockUpdatedDeveloper();
        developerInput.setUsername("        ");
        mockDeveloperAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> developerService.updateLoggedUser(developerInput));
        assertEquals("Username cannot be empty!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateDeveloper_Authenticated_NoMinLengthUsername() {
        Developer developerInput = mockUpdatedDeveloper();
        developerInput.setUsername("dev");
        mockDeveloperAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> developerService.updateLoggedUser(developerInput));
        assertEquals("Username must be at least 5 characters long!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateDeveloper_Authenticated_NullPassword() {
        Developer developerInput = mockUpdatedDeveloper();
        developerInput.setPassword(null);
        mockDeveloperAuth();

        developerService.updateLoggedUser(developerInput);

        verify(userRepository, times(1)).save(
                argThat(dev
                        -> dev.getUsername().equals(developerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(mockDeveloper().getPassword(), dev.getPassword())
                        && dev.getEmail().equals(developerInput.getEmail())
                        && dev.getRole().equals(UserEntity.Role.DEVELOPER)
                        && dev.getDetails().getStudio().equals(developerInput.getStudio())
                        && dev.getDetails().getWebsite().equals(developerInput.getWebsite())
                        && dev.getDetails().getFirstName() == null
                        && dev.getDetails().getLastName() == null
                )
        );
    }

    @Test
    void testUpdateDeveloper_Authenticated_NoUppercaseLetterPassword() {
        Developer developerInput = mockUpdatedDeveloper();
        developerInput.setPassword("pass");
        mockDeveloperAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> developerService.updateLoggedUser(developerInput));
        assertEquals("Password must contain at least one uppercase letter!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateDeveloper_Authenticated_NoLowercaseLetterPassword() {
        // TODO
    }

    @Test
    void testUpdateDeveloper_Authenticated_NoDigitPassword() {
        // TODO
    }

    @Test
    void testUpdateDeveloper_Authenticated_NoMinLengthPassword() {
        // TODO
    }

    @Test
    void testUpdateDeveloper_Authenticated_NullEmail() {
        Developer developerInput = mockUpdatedDeveloper();
        developerInput.setEmail(null);
        mockDeveloperAuth();

        developerService.updateLoggedUser(developerInput);

        verify(userRepository, times(1)).save(
                argThat(dev
                        -> dev.getUsername().equals(developerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(developerInput.getPassword(), dev.getPassword())
                        && dev.getEmail().equals(mockDeveloper().getEmail())
                        && dev.getRole().equals(UserEntity.Role.DEVELOPER)
                        && dev.getDetails().getStudio().equals(developerInput.getStudio())
                        && dev.getDetails().getWebsite().equals(developerInput.getWebsite())
                        && dev.getDetails().getFirstName() == null
                        && dev.getDetails().getLastName() == null
                )
        );
    }

    @Test
    void testUpdateDeveloper_Authenticated_NonUniqueEmail() {
        // TODO
    }

    @Test
    void testUpdateDeveloper_Authenticated_NoFormatEmail() {
        // TODO
    }

    @Test
    void testUpdateCustomer_Authenticated_NullStudio() {
        // TODO
    }

    @Test
    void testUpdateDeveloper_Authenticated_BlankStudio() {
        // TODO
    }

    @Test
    void testUpdateDeveloper_Authenticated_NullWebsite() {
        // TODO
    }

    @Test
    void testUpdateDeveloper_Authenticated_InvalidFormatWebsite() {
        // TODO
    }

    @Test
    void testUpdateDeveloper_Unauthenticated() {
        // Developer developerInput = mockDeveloper();
        // SecurityContextHolder.getContext().setAuthentication(null);

        // TODO: UnauthorizedAccessException not thrown
        // assertThrows(UnauthorizedAccessException.class, () -> developerService.updateLoggedUser(developerInput));
    }

}
