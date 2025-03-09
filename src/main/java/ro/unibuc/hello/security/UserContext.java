package ro.unibuc.hello.security;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import ro.unibuc.hello.data.entity.UserEntity;

public class UserContext {

    private static final String USER_ATTRIBUTE = "authenticatedUser";

    public static void setUser(UserEntity user) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.setAttribute(USER_ATTRIBUTE, user, RequestAttributes.SCOPE_REQUEST);
        }
    }

    public static UserEntity getUser() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return (UserEntity) attributes.getAttribute(USER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        }
        return null;
    }

}
