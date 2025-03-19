package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.annotation.DeveloperOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Developer;
import ro.unibuc.hello.service.DeveloperService;

import java.util.List;

import static ro.unibuc.hello.utils.ResponseUtils.*;

@Controller
@RequestMapping("/developers")
public class DeveloperController {

    @Autowired
    private DeveloperService developerService;

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<UserEntity> getDeveloperById(@PathVariable String id) {
        return ok(developerService.getUserById(id));
    }

    @GetMapping("/{id}/games")
    @ResponseBody
    public ResponseEntity<List<GameEntity>> getDeveloperGames(@PathVariable String id) {
        return ok(developerService.getGames(id));
    }

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<List<UserEntity>> getAllDevelopers() {
        return ok(developerService.getAllUsers());
    }

    @GetMapping("/myGames")
    @ResponseBody
    @DeveloperOnly
    public ResponseEntity<List<GameEntity>> getMyGames() {
        return ok(developerService.getGames());
    }

    @PutMapping("")
    @ResponseBody
    @DeveloperOnly
    public ResponseEntity<UserEntity> updateLoggedDeveloper(@RequestBody Developer developer) {
        return ok(developerService.updateLoggedUser(developer));
    }

}
