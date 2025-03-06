package ro.unibuc.hello.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ro.unibuc.hello.data.CustomerEntity;
import ro.unibuc.hello.data.DeveloperEntity;
import ro.unibuc.hello.dto.*;
import ro.unibuc.hello.service.AuthenticationService;

@Controller
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("/login")
    @ResponseBody
    public LoginResult login(@Valid @RequestBody LoginInput loginInput) {
        return authenticationService.login(loginInput);
    }

    @PostMapping("/signup/developer")
    @ResponseBody
    public DeveloperEntity signupDeveloper(@Valid @RequestBody DeveloperInput developerInput) {
        return authenticationService.signupDeveloper(developerInput);
    }

    @PostMapping("/signup/customer")
    @ResponseBody
    public CustomerEntity signupCustomer(@Valid @RequestBody CustomerInput customerInput) {
        return authenticationService.signupCustomer(customerInput);
    }

}
