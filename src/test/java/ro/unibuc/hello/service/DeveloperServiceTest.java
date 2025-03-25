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
        resetAccessToken();
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
    void testUpdateDeveloper_AuthenticatedValid() {
        Developer developerInput = mockUpdatedDeveloperInput();
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
        Developer developerInput = mockUpdatedDeveloperInput();
        developerInput.setUsername(null);
        UserEntity mockDeveloper = mockDeveloperAuth();

        developerService.updateLoggedUser(developerInput);

        verify(userRepository, times(1)).save(
                argThat(dev
                        -> dev.getUsername().equals(mockDeveloper.getUsername())
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
        UserEntity mockDeveloper = mockDeveloperAuth();
        Developer developerInput = mockUpdatedDeveloperInput();
        developerInput.setUsername(mockDeveloper.getUsername());

        when(userRepository.findByUsername(mockDeveloper.getUsername())).thenReturn(mockDeveloper);

        ValidationException exception = assertThrows(ValidationException.class, () -> developerService.updateLoggedUser(developerInput));
        assertEquals("Username " + developerInput.getUsername() + " already exists!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateDeveloper_Authenticated_BlankUsername() {
        Developer developerInput = mockUpdatedDeveloperInput();
        developerInput.setUsername("        ");
        mockDeveloperAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> developerService.updateLoggedUser(developerInput));
        assertEquals("Username cannot be empty!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateDeveloper_Authenticated_NoMinLengthUsername() {
        Developer developerInput = mockUpdatedDeveloperInput();
        developerInput.setUsername("dev");
        mockDeveloperAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> developerService.updateLoggedUser(developerInput));
        assertEquals("Username must be at least 5 characters long!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateDeveloper_Authenticated_NullPassword() {
        Developer developerInput = mockUpdatedDeveloperInput();
        developerInput.setPassword(null);
        mockDeveloperAuth();

        developerService.updateLoggedUser(developerInput);

        verify(userRepository, times(1)).save(
                argThat(dev
                        -> dev.getUsername().equals(developerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(mockDeveloperInput().getPassword(), dev.getPassword())
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
        Developer developerInput = mockUpdatedDeveloperInput();
        developerInput.setPassword("pass");
        mockDeveloperAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> developerService.updateLoggedUser(developerInput));
        assertEquals("Password must contain at least one uppercase letter!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateDeveloper_Authenticated_NullEmail() {
        Developer developerInput = mockUpdatedDeveloperInput();
        developerInput.setEmail(null);
        UserEntity mockDeveloper = mockDeveloperAuth();

        developerService.updateLoggedUser(developerInput);

        verify(userRepository, times(1)).save(
                argThat(dev
                        -> dev.getUsername().equals(developerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(developerInput.getPassword(), dev.getPassword())
                        && dev.getEmail().equals(mockDeveloper.getEmail())
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
        UserEntity mockDeveloper = mockDeveloperAuth();
        Developer developerInput = mockUpdatedDeveloperInput();
        developerInput.setEmail(mockDeveloper.getEmail());

        when(userRepository.findByEmail(mockDeveloper.getEmail())).thenReturn(mockDeveloper);

        ValidationException exception = assertThrows(ValidationException.class, () -> developerService.updateLoggedUser(developerInput));
        assertEquals("Email " + developerInput.getEmail() + " already exists!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateDeveloper_Authenticated_NoFormatEmail() {
        Developer developerInput = mockUpdatedDeveloperInput();
        developerInput.setEmail("invalid-format");
        mockDeveloperAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> developerService.updateLoggedUser(developerInput));
        assertEquals("Email must be a valid email address!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateDeveloper_Authenticated_NullStudio() {
        Developer developerInput = mockUpdatedDeveloperInput();
        developerInput.setStudio(null);
        UserEntity mockDeveloper = mockDeveloperAuth();

        developerService.updateLoggedUser(developerInput);

        verify(userRepository, times(1)).save(
                argThat(dev
                        -> dev.getUsername().equals(developerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(developerInput.getPassword(), dev.getPassword())
                        && dev.getEmail().equals(developerInput.getEmail())
                        && dev.getRole().equals(UserEntity.Role.DEVELOPER)
                        && dev.getDetails().getStudio().equals(mockDeveloper.getDetails().getStudio())
                        && dev.getDetails().getWebsite().equals(developerInput.getWebsite())
                        && dev.getDetails().getFirstName() == null
                        && dev.getDetails().getLastName() == null
                )
        );
    }

    @Test
    void testUpdateDeveloper_Authenticated_NonUniqueStudio() {
        UserEntity mockDeveloper = mockDeveloperAuth();
        Developer developerInput = mockUpdatedDeveloperInput();
        developerInput.setStudio(mockDeveloper.getDetails().getStudio());

        when(userRepository.findByDetailsStudio(mockDeveloper.getDetails().getStudio())).thenReturn(mockDeveloper);

        ValidationException exception = assertThrows(ValidationException.class, () -> developerService.updateLoggedUser(developerInput));
        assertEquals("Studio " + developerInput.getStudio() + " already exists!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateDeveloper_Authenticated_BlankStudio() {
        Developer developerInput = mockUpdatedDeveloperInput();
        developerInput.setStudio("   \n    ");
        mockDeveloperAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> developerService.updateLoggedUser(developerInput));
        assertEquals("Studio cannot be empty!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void testUpdateDeveloper_Authenticated_NullWebsite() {
        Developer developerInput = mockUpdatedDeveloperInput();
        developerInput.setWebsite(null);
        UserEntity mockDeveloper = mockDeveloperAuth();

        developerService.updateLoggedUser(developerInput);

        verify(userRepository, times(1)).save(
                argThat(dev
                        -> dev.getUsername().equals(developerInput.getUsername())
                        && AuthenticationUtils.isPasswordValid(developerInput.getPassword(), dev.getPassword())
                        && dev.getEmail().equals(developerInput.getEmail())
                        && dev.getRole().equals(UserEntity.Role.DEVELOPER)
                        && dev.getDetails().getStudio().equals(developerInput.getStudio())
                        && dev.getDetails().getWebsite().equals(mockDeveloper.getDetails().getWebsite())
                        && dev.getDetails().getFirstName() == null
                        && dev.getDetails().getLastName() == null
                )
        );
    }

    @Test
    void testUpdateDeveloper_Authenticated_NoFormatWebsite() {
        Developer developerInput = mockUpdatedDeveloperInput();
        developerInput.setWebsite("website.invalid");
        mockDeveloperAuth();

        ValidationException exception = assertThrows(ValidationException.class, () -> developerService.updateLoggedUser(developerInput));
        assertEquals("Website must be a valid website URL!", exception.getMessage());

        verify(userRepository, times(0)).save(any());
    }

}
