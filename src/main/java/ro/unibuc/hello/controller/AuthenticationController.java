package ro.unibuc.hello.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import ro.unibuc.hello.dto.CustomerInput;
import ro.unibuc.hello.dto.DeveloperInput;
import ro.unibuc.hello.dto.LoginInput;
import ro.unibuc.hello.dto.LoginResult;

@Controller
public class AuthenticationController {

    @PostMapping("/auth/login")
    @ResponseBody
    public LoginResult login(@Valid @RequestBody LoginInput loginInput) {
        return new LoginResult("LOGIN TEST: "
                + " --- Username: " + loginInput.getUsername()
                + " --- Password: " + loginInput.getPassword()
        );
    }

    @PostMapping("auth/signup/developer")
    @ResponseBody
    public String signupDeveloper(@Valid @RequestBody DeveloperInput developerInput) {
        return "Signup Developer: "
                + " --- Username: " + developerInput.getUsername()
                + " --- Password: " + developerInput.getPassword()
                + " --- Email: " + developerInput.getEmail()
                + " --- Studio: " + developerInput.getStudio()
                + " --- Website: " + developerInput.getWebsite();
    }

    @PostMapping("auth/signup/customer")
    @ResponseBody
    public String signupCustomer(@Valid @RequestBody CustomerInput customerInput) {
        return "Signup Customer: "
                + " --- Username: " + customerInput.getUsername()
                + " --- Password: " + customerInput.getPassword()
                + " --- Email: " + customerInput.getEmail()
                + " --- FirstName: " + customerInput.getFirstName()
                + " --- LastName: " + customerInput.getLastName();
    }

}
