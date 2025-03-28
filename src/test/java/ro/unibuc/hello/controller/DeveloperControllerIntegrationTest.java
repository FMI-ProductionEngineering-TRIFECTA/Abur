package ro.unibuc.hello.controller;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.utils.GenericControllerIntegrationTest;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.mockDeveloperInput;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.mockUpdatedDeveloperInput;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("IntegrationTest")
public class DeveloperControllerIntegrationTest extends GenericControllerIntegrationTest<DeveloperController> {

    @Autowired
    UserRepository userRepository;

    @Autowired
    DeveloperController developerController;

    @Override
    public String getEndpoint() {
        return "developers";
    }

    @Override
    public DeveloperController getController() {
        return developerController;
    }

    @Test
    void testGetDeveloperById_ValidId() throws Exception {
        UserEntity developerDB = userRepository.findByIdAndRole(getUserId(Role.DEVELOPER), Role.DEVELOPER);

        performGet(null,"/{id}", developerDB.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(developerDB.getId()))
                .andExpect(jsonPath("$.username").value(developerDB.getUsername()))
                .andExpect(jsonPath("$.password").value(developerDB.getPassword()))
                .andExpect(jsonPath("$.email").value(developerDB.getEmail()))
                .andExpect(jsonPath("$.details.studio").value(developerDB.getDetails().getStudio()))
                .andExpect(jsonPath("$.details.website").value(developerDB.getDetails().getWebsite()))
                .andExpect(jsonPath("$.details.firstName").doesNotExist())
                .andExpect(jsonPath("$.details.lastName").doesNotExist());
    }

    @Test
    void testGetDeveloperById_InvalidId() throws Exception {
        final String id = "id-invalid";

        performGet(null, "/{id}", id)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(String.format("No %s found at id %s", Role.DEVELOPER.toString().toLowerCase(), id)));
    }

    @Test
    void testGetDeveloperGames_ValidId() throws Exception {
        UserEntity developerDB = userRepository.findByIdAndRole(getUserId(Role.DEVELOPER), Role.DEVELOPER);

        matchAllGames(
                "",
                performGet(null, "/{id}/games", developerDB.getId())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(developerDB.getGames().size()))),
                developerDB.getGames()
        );
    }

    @Test
    void testGetDeveloperGames_InvalidId() throws Exception {
        final String id = "id-invalid";

        performGet(null, "/{id}/games", id)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(String.format("No %s found at id %s", Role.DEVELOPER.toString().toLowerCase(), id)));
    }

    @Test
    void testGetAllDevelopers() throws Exception {
        List<UserEntity> developersDB = userRepository.findAllByRole(Role.DEVELOPER);

        matchAllDevelopers(
                "",
                performGet(null, "")
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(developersDB.size()))),
                developersDB
        );
    }

    @Test
    void testGetMyGames_Authenticated() throws Exception {
        UserEntity developerDB = userRepository.findByIdAndRole(getUserId(Role.DEVELOPER), Role.DEVELOPER);

        matchAllGames(
                "",
                performGet(getAccessToken(Role.DEVELOPER), "/myGames")
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(developerDB.getGames().size()))),
                developerDB.getGames()
        );
    }

    @Test
    void testGetMyGames_Unauthenticated() throws Exception {
        performGet("/myGames")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetMyGames_AuthenticatedCustomer() throws Exception {
        performGet(getAccessToken(Role.CUSTOMER), "/myGames")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateLoggedDeveloper_Authenticated() throws Exception {
        Developer developerInput = mockUpdatedDeveloperInput();
        UserEntity developerDB = userRepository.findByIdAndRole(getUserId(Role.DEVELOPER), Role.DEVELOPER);

        performPut(developerInput, getAccessToken(Role.DEVELOPER), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(developerDB.getId()))
                .andExpect(jsonPath("$.username").value(developerInput.getUsername()))
                .andExpect(jsonPath("$.email").value(developerInput.getEmail()))
                .andExpect(jsonPath("$.details.studio").value(developerInput.getStudio()))
                .andExpect(jsonPath("$.details.website").value(developerInput.getWebsite()))
                .andExpect(jsonPath("$.details.firstName").doesNotExist())
                .andExpect(jsonPath("$.details.lastName").doesNotExist());
    }

    @Test
    void testUpdateLoggedDeveloper_AuthenticatedCustomer() throws Exception {
        performPut(mockDeveloperInput(), getAccessToken(Role.CUSTOMER), "")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateLoggedDeveloper_Unauthenticated() throws Exception {
        performPut(mockDeveloperInput(), null, "")
                .andExpect(status().isUnauthorized());
    }

}
