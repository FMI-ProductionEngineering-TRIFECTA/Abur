package ro.unibuc.hello.utils;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.hello.aspect.RoleAuthorizationAspect;
import ro.unibuc.hello.exception.GlobalExceptionHandler;

import static ro.unibuc.hello.utils.AuthenticationTestUtils.resetMockedAccessToken;

public abstract class GenericControllerTest<C> implements ControllerTestInterface<C> {

    protected MockMvc mockMvc;

    @BeforeEach
    protected void setUp() {
        resetMockedAccessToken();

        AspectJProxyFactory factory = new AspectJProxyFactory(getController());
        factory.addAspect(new RoleAuthorizationAspect());
        mockMvc = MockMvcBuilders
            .standaloneSetup(getController().getClass().cast(factory.getProxy()))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Override
    public MockMvc getMockMvc() {
        return mockMvc;
    }
}
