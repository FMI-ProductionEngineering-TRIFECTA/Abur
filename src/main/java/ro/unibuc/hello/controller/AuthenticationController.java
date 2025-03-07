package ro.unibuc.hello.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.CustomerInput;
import ro.unibuc.hello.dto.DeveloperInput;
import ro.unibuc.hello.dto.LoginInput;
import ro.unibuc.hello.dto.LoginResult;
import ro.unibuc.hello.security.AuthenticationService;

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
    public UserEntity signupDeveloper(@Valid @RequestBody DeveloperInput developerInput) {
        return authenticationService.signupDeveloper(developerInput);
    }

    @PostMapping("/signup/customer")
    @ResponseBody
    public UserEntity signupCustomer(@Valid @RequestBody CustomerInput customerInput) {
        return authenticationService.signupCustomer(customerInput);
    }

}
