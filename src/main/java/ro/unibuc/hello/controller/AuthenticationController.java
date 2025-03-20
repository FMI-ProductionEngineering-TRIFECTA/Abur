package ro.unibuc.hello.controller;

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
import ro.unibuc.hello.dto.Token;
import ro.unibuc.hello.service.AuthenticationService;

import static ro.unibuc.hello.utils.ResponseUtils.*;

@Controller
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Token> login(@RequestBody Credentials credentials) {
        return ok(authenticationService.login(credentials));
    }

    @PostMapping("/signup/developer")
    @ResponseBody
    public ResponseEntity<UserEntity> signupDeveloper(@RequestBody Developer developer) {
        return created(authenticationService.signupDeveloper(developer));
    }

    @PostMapping("/signup/customer")
    @ResponseBody
    public ResponseEntity<UserEntity> signupCustomer(@RequestBody Customer customer) {
        return created(authenticationService.signupCustomer(customer));
    }

}
