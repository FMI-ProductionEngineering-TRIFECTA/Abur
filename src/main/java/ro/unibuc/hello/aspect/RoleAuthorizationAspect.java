package ro.unibuc.hello.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.annotation.DeveloperOnly;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.security.AuthenticationUtils;

import static ro.unibuc.hello.utils.ResponseUtils.unauthorized;

@Aspect
@Component
public class RoleAuthorizationAspect {

    @Around("@annotation(developerOnly)")
    public ResponseEntity<?> checkDeveloperAuthorization(ProceedingJoinPoint joinPoint, DeveloperOnly developerOnly) throws Throwable {
        return checkAuthorization(joinPoint, UserEntity.Role.DEVELOPER);
    }

    @Around("@annotation(customerOnly)")
    public ResponseEntity<?> checkCustomerAuthorization(ProceedingJoinPoint joinPoint, CustomerOnly customerOnly) throws Throwable {
        return checkAuthorization(joinPoint, UserEntity.Role.CUSTOMER);
    }

    private ResponseEntity<?> checkAuthorization(ProceedingJoinPoint joinPoint, UserEntity.Role requiredRole) throws Throwable {
        UserEntity user = AuthenticationUtils.getAuthorizedUser(requiredRole);
        if (user == null) {
            return unauthorized();
        }

        return (ResponseEntity<?>) joinPoint.proceed();
    }
}
