package ro.unibuc.hello.utils;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.addToken;

public interface ControllerTestInterface<C> {

    String ID = "Invalid ID";
    Integer MIN_STRING_VALID_LENGTH = 5;

    MockMvc getMockMvc();
    String getEndpoint();
    C getController();

    private String formatEndpoint(String restOfEndpoint) {
        return String.format("/%s%s", getEndpoint(), restOfEndpoint);
    }

    private ResultActions performJsonRequest(MockHttpServletRequestBuilder requestBuilder, Object requestBody, String token) throws Exception {
        return getMockMvc()
                .perform(requestBuilder
                        .content(new ObjectMapper().writeValueAsString(requestBody))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(addToken(token)));
    }

    default ResultActions performGet(String token, String endpointTemplate, Object... args) throws Exception {
        return getMockMvc().perform(get(formatEndpoint(endpointTemplate), args)
                .with(addToken(token)));
    }

    default ResultActions performGet(String endpoint) throws Exception {
        return performGet(null, endpoint, new Object[0]);
    }

    default ResultActions performGet() throws Exception {
        return performGet("");
    }

    default ResultActions performPost(Object requestBody, String token, String endpointTemplate, Object... args) throws Exception {
        return performJsonRequest(post(formatEndpoint(endpointTemplate), args), requestBody, token);
    }

    default ResultActions performPost(Object requestBody, String token, String endpoint) throws Exception {
        return performPost(requestBody, token, endpoint, new Object[0]);
    }

    default ResultActions performPost(Object requestBody, String token) throws Exception {
        return performPost(requestBody, token, "");
    }

    default ResultActions performPut(Object requestBody, String token, String endpointTemplate, Object... args) throws Exception {
        return performJsonRequest(put(formatEndpoint(endpointTemplate), args), requestBody, token);
    }

    default ResultActions performPut(Object requestBody, String token, String endpoint) throws Exception {
        return performPut(requestBody, token, endpoint, new Object[0]);
    }

    default ResultActions performDelete(String token, String endpointTemplate, Object... args) throws Exception {
        return getMockMvc()
                .perform(delete(formatEndpoint(endpointTemplate), args)
                        .with(addToken(token)));
    }

    default ResultActions performDelete(String token, String endpoint) throws Exception {
        return performDelete(token, endpoint, new Object[0]);
    }

    private static <T> ResultMatcher match(String jsonTemplate, String jsonPrefix, List<T> entities, Map<String, Function<T, Object>> fieldGetters) {
        return result -> {
            for (int i = 0; i < entities.size(); ++i) {
                for (Map.Entry<String, Function<T, Object>> entry : fieldGetters.entrySet()) {
                    String jsonPathExpr = jsonTemplate.contains("[%d]")
                            ? String.format(jsonTemplate, jsonPrefix, i, entry.getKey())
                            : String.format(jsonTemplate, jsonPrefix, entry.getKey());
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

    default <T> ResultMatcher matchAll(String jsonPrefix, List<T> entities, Map<String, Function<T, Object>> fieldGetters) {
        return match("$%s[%d].%s", jsonPrefix, entities, fieldGetters);
    }

    default <T> ResultMatcher matchAll(List<T> entities, Map<String, Function<T, Object>> fieldGetters) {
        return matchAll("", entities, fieldGetters);
    }

    default <T> ResultMatcher matchOne(T entity, Map<String, Function<T, Object>> fieldGetters) {
        return match("$%s.%s", "", List.of(entity), fieldGetters);
    }

    Map<String, Function<GameEntity, Object>> GAME_FIELDS = Map.of(
            "id", GameEntity::getId,
            "title", GameEntity::getTitle,
            "price", GameEntity::getPrice,
            "discountPercentage", GameEntity::getDiscountPercentage,
            "keys", GameEntity::getKeys,
            "type", game -> game.getType().toString()
    );

    Map<String, Function<UserEntity, Object>> DEVELOPER_FIELDS = Map.of(
            "id", UserEntity::getId,
            "username", UserEntity::getUsername,
            "password", UserEntity::getPassword,
            "email", UserEntity::getEmail,
            "details.studio", user -> user.getDetails().getStudio(),
            "details.website", user -> user.getDetails().getWebsite(),
            "details.firstName", user -> null,
            "details.lastName", user -> null
    );

     Map<String, Function<UserEntity, Object>> CUSTOMER_FIELDS = Map.of(
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
