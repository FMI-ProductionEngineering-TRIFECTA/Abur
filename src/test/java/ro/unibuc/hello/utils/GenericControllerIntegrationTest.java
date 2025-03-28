package ro.unibuc.hello.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import ro.unibuc.hello.config.DatabaseSeeder;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.security.jwt.JWTService;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static ro.unibuc.hello.utils.DatabaseUtils.getId;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("IntegrationTest")
public abstract class GenericControllerIntegrationTest<C> implements ControllerTestInterface<C> {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DatabaseSeeder databaseSeeder;

    @Autowired
    private JWTService jwtService;

    @BeforeEach
    protected void seeder() {
        databaseSeeder.seedData();
    }

    @Override
    public MockMvc getMockMvc() {
        return mockMvc;
    }

    public static String getGameId(Integer id) {
        return getId("games", id);
    }

    public static String getUserId(UserEntity.Role role) {
        return getId(role == UserEntity.Role.CUSTOMER ? "customers" : "developers", 0);
    }

    public String getAccessToken(UserEntity.Role role) {
        return jwtService.getToken(getUserId(role));
    }

}
