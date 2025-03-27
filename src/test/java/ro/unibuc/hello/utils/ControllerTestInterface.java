package ro.unibuc.hello.utils;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.addToken;

public interface ControllerTestInterface<C> {

    String ID = "Invalid ID";

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

    default ResultActions performDelete(String token) throws Exception {
        return performDelete(token, "");
    }
}
