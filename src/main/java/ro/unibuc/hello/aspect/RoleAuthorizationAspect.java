package ro.unibuc.hello.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import ro.unibuc.hello.exception.UnauthorizedAccessException;

import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.security.AuthenticationUtils.getUser;

@Aspect
@Component
public class RoleAuthorizationAspect {

    @Before("@annotation(ro.unibuc.hello.annotation.DeveloperOnly)")
    public void checkDeveloperAuthorization() {
        checkAuthorization(Role.DEVELOPER);
    }

    @Before("@annotation(ro.unibuc.hello.annotation.CustomerOnly)")
    public void checkCustomerAuthorization() {
        checkAuthorization(Role.CUSTOMER);
    }

    private void checkAuthorization(Role requiredRole) {
        if (getUser().getRole() != requiredRole) {
            throw new UnauthorizedAccessException();
        }
    }

}
