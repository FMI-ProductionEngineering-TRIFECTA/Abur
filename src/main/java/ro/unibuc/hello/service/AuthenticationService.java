package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.*;
import ro.unibuc.hello.security.AuthenticationUtils;
import ro.unibuc.hello.security.jwt.JWTService;

import static ro.unibuc.hello.utils.ValidationUtils.*;
import static ro.unibuc.hello.utils.ResponseUtils.*;

@Service
@SuppressWarnings("unchecked")
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTService jwtService;

    private static ResponseEntity<ErrorString> err;

    public ResponseEntity<?> login(Credentials credentials) {
        err = chain
        (
            exists("Username", credentials.getUsername()),
            exists("Password", credentials.getPassword()),
            validate("Username", credentials.getUsername()),
            validate("Password", credentials.getPassword(), validPassword().and(validLength(5)))
        );
        if (err != null) return err;

        UserEntity user = userRepository.findByUsername(credentials.getUsername());

        if (user != null && AuthenticationUtils.isPasswordValid(credentials.getPassword(), user.getPassword())) {
            return ok(new Token(jwtService.getToken(user.getId())));
        }

        return badRequest("User %s doesn't exist", credentials.getUsername());
    }

    public ResponseEntity<?> signupDeveloper(Developer developer) {
        // TODO: Add validation

        return created(userRepository.save(new UserEntity(
                developer.getUsername(),
                developer.getPassword(),
                developer.getEmail(),
                UserEntity.Role.DEVELOPER,
                UserEntity.UserDetails.forDeveloper(
                        developer.getStudio(),
                        developer.getWebsite()
                )
        )));
    }

    public ResponseEntity<?> signupCustomer(Customer customer) {
        // TODO: Add validation

        return created(userRepository.save(new UserEntity(
                customer.getUsername(),
                customer.getPassword(),
                customer.getEmail(),
                UserEntity.Role.CUSTOMER,
                UserEntity.UserDetails.forCustomer(
                        customer.getFirstName(),
                        customer.getLastName()
                )
        )));
    }

}
