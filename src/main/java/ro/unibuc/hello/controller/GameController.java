package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.service.GameService;

@Controller
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameService gameService;

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getGameById(@PathVariable String id) {
        return gameService.getGameById(id);
    }

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<?> getGames() {
        return gameService.getAllGames();
    }

    @PostMapping("")
    @ResponseBody
    public ResponseEntity<?> createGame(@RequestBody Game game) {
        return gameService.createGame(game);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateGame(@PathVariable String id, @RequestBody Game game) {
        return gameService.updateGame(id, game);
    }

    @PutMapping("/{id}/addKeys")
    @ResponseBody
    public ResponseEntity<?> addKeys(@PathVariable String id, @RequestParam("quantity") Integer quantity) {
        return gameService.addKeys(id, quantity);
    }

    @PutMapping("/{id}/markOutOfStock")
    @ResponseBody
    public ResponseEntity<?> markOutOfSpot(@PathVariable String id) {
        return gameService.markOutOfStock(id);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteGame(@PathVariable String id) {
        return gameService.deleteGame(id);
    }

}
