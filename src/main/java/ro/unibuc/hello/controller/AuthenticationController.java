package ro.unibuc.hello.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Credentials;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.service.AuthenticationService;

@Controller
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Credentials credentials) {
        return authenticationService.login(credentials);
    }

    @PostMapping("/signup/developer")
    @ResponseBody
    public UserEntity signupDeveloper(@Valid @RequestBody Developer developer) {
        return authenticationService.signupDeveloper(developer);
    }

    @PostMapping("/signup/customer")
    @ResponseBody
    public UserEntity signupCustomer(@Valid @RequestBody Customer customer) {
        return authenticationService.signupCustomer(customer);
    }

}
