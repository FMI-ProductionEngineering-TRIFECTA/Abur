package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.CustomerEntity;
import ro.unibuc.hello.data.CustomerRepository;
import ro.unibuc.hello.data.DeveloperEntity;
import ro.unibuc.hello.data.DeveloperRepository;
import ro.unibuc.hello.dto.*;

@Service
public class AuthenticationService {

    @Autowired
    JWTService jwtService;

    @Autowired
    DeveloperRepository developerRepository;

    @Autowired
    CustomerRepository customerRepository;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public enum AccessType {
        CUSTOMER,
        DEVELOPER,
        ALL
    }

    public LoginResult login(LoginInput loginInput) {
        // TODO: refactor - dupa ce vedem cum retinem datele in mongo DB
        DeveloperEntity developer = developerRepository.findByUsername(loginInput.getUsername());
        CustomerEntity customer = customerRepository.findByUsername(loginInput.getUsername());

        // TODO: refactor - dupa ce vedem cum retinem datele in mongo DB
        if (developer != null && isPasswordValid(loginInput.getPassword(), developer.getPassword())) {
            return new LoginResult(jwtService.getToken(developer.getId()));
        } else if (customer != null && isPasswordValid(loginInput.getPassword(), customer.getPassword())) {
            return new LoginResult(jwtService.getToken(customer.getId()));
        }

        return new LoginResult(null);
    }

    public DeveloperEntity signupDeveloper(DeveloperInput developerInput) {
        return developerRepository.save(new DeveloperEntity(
                developerInput.getUsername(),
                encryptPassword(developerInput.getPassword()),
                developerInput.getEmail(),
                developerInput.getStudio(),
                developerInput.getWebsite()
        ));
    }

    public CustomerEntity signupCustomer(CustomerInput customerInput) {
        return customerRepository.save(new CustomerEntity(
                customerInput.getUsername(),
                encryptPassword(customerInput.getPassword()),
                customerInput.getEmail(),
                customerInput.getFirstName(),
                customerInput.getLastName()
        ));
    }

    public static String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean verifyAccess(AccessType access) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String userId) {
            return switch (access) {
                case CUSTOMER -> (customerRepository.findById(userId)).isPresent();
                case DEVELOPER -> (developerRepository.findById(userId)).isPresent();
                case ALL -> true;
            };
        }

        return false;
    }

    private static boolean isPasswordValid(String providedPassword, String actualPassword) {
        return passwordEncoder.matches(providedPassword, actualPassword);
    }

}
