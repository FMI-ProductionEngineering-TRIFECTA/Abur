package ro.unibuc.hello.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ro.unibuc.hello.config.DatabaseSeeder;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.security.jwt.JWTService;

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

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.20")
            .withExposedPorts(27017)
            .withSharding();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        final String MONGO_URL = "mongodb://localhost:";
        final String PORT = String.valueOf(mongoDBContainer.getMappedPort(27017));

        registry.add("mongodb.connection.url", () -> MONGO_URL + PORT);
    }

    @Override
    public MockMvc getMockMvc() {
        return mockMvc;
    }

    public static String getUserId(UserEntity.Role role) {
        return getId(role == UserEntity.Role.CUSTOMER ? "customers" : "developers", 0);
    }

    public String getAccessToken(UserEntity.Role role) {
        return jwtService.getToken(getUserId(role));
    }

    @BeforeEach
    protected void setUp() {
        databaseSeeder.seedData();
    }

    @BeforeAll
    public static void initialSetUp() {
        mongoDBContainer.start();
    }

    @AfterAll
    public static void tearDown() {
        mongoDBContainer.stop();
    }

}