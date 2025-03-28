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
import org.springframework.test.web.servlet.ResultMatcher;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ro.unibuc.hello.config.DatabaseSeeder;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.security.jwt.JWTService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.Matchers.equalTo;
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

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.20")
            .withExposedPorts(27017)
            .withSharding();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("mongodb.connection.url", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeAll
    public static void initialSetUp() {
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
    }

    @AfterAll
    public static void tearDown() {
         mongoDBContainer.stop();
    }

    @BeforeEach
    protected void setUp() {
        databaseSeeder.seedData();
    }

    @Override
    public MockMvc getMockMvc() {
        return mockMvc;
    }

    public static String getGameAtId(Integer id) {
        return getId("games", id);
    }

    public static String getUserId(UserEntity.Role role) {
        return getId(role == UserEntity.Role.CUSTOMER ? "customers" : "developers", 0);
    }

    public String getAccessToken(UserEntity.Role role) {
        return jwtService.getToken(getUserId(role));
    }

    public static <T> ResultMatcher matchAll(String jsonPrefix, List<T> entities, Map<String, Function<T, Object>> fieldGetters) {
        return result -> {
            for (int i = 0; i < entities.size(); ++i) {
                for (Map.Entry<String, Function<T, Object>> entry : fieldGetters.entrySet()) {
                    String jsonPathExpr = String.format("$%s[%d].%s", jsonPrefix, i, entry.getKey());
                    Object expectedValue = entry.getValue().apply(entities.get(i));
                    if (expectedValue != null) {
                        jsonPath(jsonPathExpr, equalTo(expectedValue)).match(result);
                    }
                    else {
                        jsonPath(jsonPathExpr).doesNotExist().match(result);
                    }
                }
            }
        };
    }

    public static <T> ResultMatcher matchAll(List<T> entities, Map<String, Function<T, Object>> fieldGetters) {
        return matchAll("", entities, fieldGetters);
    }

    public static final Map<String, Function<GameEntity, Object>> GAME_FIELDS = Map.of(
            "id", GameEntity::getId,
            "title", GameEntity::getTitle,
            "price", GameEntity::getPrice,
            "discountPercentage", GameEntity::getDiscountPercentage,
            "keys", GameEntity::getKeys,
            "type", game -> game.getType().toString()
    );

    public static final Map<String, Function<UserEntity, Object>> DEVELOPER_FIELDS = Map.of(
            "id", UserEntity::getId,
            "username", UserEntity::getUsername,
            "password", UserEntity::getPassword,
            "email", UserEntity::getEmail,
            "details.studio", user -> user.getDetails().getStudio(),
            "details.website", user -> user.getDetails().getWebsite(),
            "details.firstName", user -> null,
            "details.lastName", user -> null
    );

    public static final Map<String, Function<UserEntity, Object>> CUSTOMER_FIELDS = Map.of(
            "id", UserEntity::getId,
            "username", UserEntity::getUsername,
            "password", UserEntity::getPassword,
            "email", UserEntity::getEmail,
            "details.firstName", user -> user.getDetails().getFirstName(),
            "details.lastName", user -> user.getDetails().getLastName(),
            "details.studio", user -> null,
            "details.website", user -> null
    );

}
