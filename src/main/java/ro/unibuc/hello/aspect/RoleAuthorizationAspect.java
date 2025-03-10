package ro.unibuc.hello.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.security.UserContext;

import static ro.unibuc.hello.security.AuthenticationUtils.*;

@Aspect
@Component
public class RoleAuthorizationAspect {

    @Before("@annotation(ro.unibuc.hello.annotation.DeveloperOnly)")
    public void checkDeveloperAuthorization() {
        checkAuthorization(UserEntity.Role.DEVELOPER);
    }

    @Before("@annotation(ro.unibuc.hello.annotation.CustomerOnly)")
    public void checkCustomerAuthorization() {
        checkAuthorization(UserEntity.Role.CUSTOMER);
    }

    private void checkAuthorization(UserEntity.Role requiredRole) {
        UserContext.setUser(getAuthorizedUser(requiredRole));
    }
}
