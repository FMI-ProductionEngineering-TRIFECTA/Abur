package ro.unibuc.hello.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ro.unibuc.hello.data.DeveloperEntity;
import ro.unibuc.hello.dto.DeveloperInput;
import ro.unibuc.hello.service.AuthenticationService;
import ro.unibuc.hello.service.DeveloperService;

import java.util.List;

@Controller
@RequestMapping("/developers")
public class DeveloperController {

    @Autowired
    DeveloperService developerService;

    @Autowired
    AuthenticationService authenticationService;

    @GetMapping("/{id}")
    @ResponseBody
    public DeveloperEntity getDeveloperById(@PathVariable String id) {
        return developerService.getDeveloperById(id);
    }

    @GetMapping("")
    @ResponseBody
    public List<DeveloperEntity> getAllDevelopers() {
        return developerService.getAllDevelopers();
    }

    @PutMapping("")
    @ResponseBody
    public DeveloperEntity updateLoggedDeveloper(@Valid @RequestBody DeveloperInput developerInput) {
        if (!authenticationService.verifyAccess(AuthenticationService.AccessType.DEVELOPER)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied");
        }
        return developerService.updateLoggedDeveloper(developerInput);
    }

}
