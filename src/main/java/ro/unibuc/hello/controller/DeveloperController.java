package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.dto.Developer;
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
    public ResponseEntity<?> updateLoggedDeveloper(@RequestBody Developer developer) {
        return developerService.updateLoggedUser(developer);
    }

}
