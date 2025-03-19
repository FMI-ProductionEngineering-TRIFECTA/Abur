package ro.unibuc.hello.utils;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ro.unibuc.hello.aspect.RoleAuthorizationAspect;
import ro.unibuc.hello.exception.GlobalExceptionHandler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static ro.unibuc.hello.utils.AuthenticationTestUtils.addToken;

@EnableAspectJAutoProxy
public abstract class GenericControllerTest<C> {

    protected MockMvc mockMvc;

    protected abstract String getEndpoint();
    protected abstract C getController();

    @BeforeEach
    protected void setUp() {
        AspectJProxyFactory factory = new AspectJProxyFactory(getController());
        factory.addAspect(new RoleAuthorizationAspect());

        mockMvc = MockMvcBuilders
                .standaloneSetup(getController().getClass().cast(factory.getProxy()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String formatEndpoint(String restOfEndpoint) {
        return String.format("/%s%s", getEndpoint(), restOfEndpoint);
    }

    protected ResultActions performGet(String endpointTemplate, Object... args) throws Exception {
        return mockMvc.perform(get(formatEndpoint(endpointTemplate), args));
    }

    protected ResultActions performGet(String endpoint) throws Exception {
        return performGet(endpoint, new Object[0]);
    }

    protected ResultActions performGet() throws Exception {
        return performGet("");
    }

    protected ResultActions performPost(Object requestBody, String token, String endpointTemplate, Object... args) throws Exception {
        return mockMvc.perform(post(formatEndpoint(endpointTemplate), args)
                .content(new ObjectMapper().writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON)
                .with(addToken(token)));
    }

    protected ResultActions performPost(Object requestBody, String token, String endpoint) throws Exception {
        return performPost(requestBody, token, endpoint, new Object[0]);
    }

    protected ResultActions performPost(Object requestBody, String token) throws Exception {
        return performPost(requestBody, token, "");
    }

}
