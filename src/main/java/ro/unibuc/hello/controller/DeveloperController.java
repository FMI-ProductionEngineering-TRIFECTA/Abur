package ro.unibuc.hello.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.security.AuthenticationUtils;
import ro.unibuc.hello.service.DeveloperService;

import java.util.List;

@Controller
@RequestMapping("/developers")
public class DeveloperController {

    @Autowired
    private DeveloperService developerService;

    @GetMapping("/{id}")
    @ResponseBody
    public UserEntity getDeveloperById(@PathVariable String id) {
        return developerService.getUserById(id);
    }

    @GetMapping("")
    @ResponseBody
    public List<UserEntity> getAllDevelopers() {
        return developerService.getAllUsers();
    }

    @PutMapping("")
    @ResponseBody
    public ResponseEntity<?> updateLoggedDeveloper(@Valid @RequestBody Developer developer) {
        if (AuthenticationUtils.hasAccess(UserEntity.Role.DEVELOPER)) {
            return developerService.updateLoggedUser(developer);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied");
    }

}
