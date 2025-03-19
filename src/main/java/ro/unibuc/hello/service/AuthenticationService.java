package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.*;
import ro.unibuc.hello.exception.ValidationException;
import ro.unibuc.hello.security.AuthenticationUtils;
import ro.unibuc.hello.security.jwt.JWTService;

import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.data.entity.UserEntity.UserDetails;
import static ro.unibuc.hello.utils.ValidationUtils.exists;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DeveloperService developerService;

    @Autowired
    private JWTService jwtService;

    private void validateSignUp(User user) {
        exists("Username", user.getUsername());
        exists("Password", user.getPassword());
        exists("Email", user.getEmail());
    }

    public Token login(Credentials credentials) {
        exists("Username", credentials.getUsername());
        exists("Password", credentials.getPassword());

        UserEntity user = userRepository.findByUsername(credentials.getUsername());

        if (user != null && AuthenticationUtils.isPasswordValid(credentials.getPassword(), user.getPassword())) {
            return new Token(jwtService.getToken(user.getId()));
        }

        throw new ValidationException("Invalid username or password");
    }

    public UserEntity signupDeveloper(Developer developer) {
        validateSignUp(developer);
        exists("Studio", developer.getStudio());
        developerService.validateUser(developer);

        return userRepository.save(new UserEntity(
                developer.getUsername(),
                developer.getPassword(),
                developer.getEmail(),
                Role.DEVELOPER,
                UserDetails.forDeveloper(
                        developer.getStudio(),
                        developer.getWebsite()
                )
        ));
    }

    public UserEntity signupCustomer(Customer customer) {
        validateSignUp(customer);
        customerService.validateUser(customer);

        return userRepository.save(new UserEntity(
                customer.getUsername(),
                customer.getPassword(),
                customer.getEmail(),
                Role.CUSTOMER,
                UserDetails.forCustomer(
                        customer.getFirstName(),
                        customer.getLastName()
                )
        ));
    }

}
