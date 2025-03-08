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

@Controller
@RequestMapping("/developers")
public class DeveloperController {

    @Autowired
    private DeveloperService developerService;

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getDeveloperById(@PathVariable String id) {
        return developerService.getUserById(id);
    }

    @GetMapping("/{id}/games")
    @ResponseBody
    public ResponseEntity<?> getDeveloperGames(@PathVariable String id) {
        return developerService.getGames(id);
    }

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<?> getAllDevelopers() {
        return developerService.getAllUsers();
    }

    @GetMapping("/myGames")
    @ResponseBody
    public ResponseEntity<?> getMyGames() {
        return developerService.getGames();
    }

    @PutMapping("")
    @ResponseBody
    public ResponseEntity<?> updateLoggedDeveloper(@Valid @RequestBody Developer developer) {
        if (AuthenticationUtils.getAuthorizedUser(UserEntity.Role.DEVELOPER) != null) {
            return developerService.updateLoggedUser(developer);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied");
    }

}
