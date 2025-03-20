package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.annotation.DeveloperOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.exception.UnauthorizedAccessException;
import ro.unibuc.hello.service.DLCService;
import ro.unibuc.hello.service.GameService;

import java.util.List;

import static ro.unibuc.hello.security.AuthenticationUtils.getUser;
import static ro.unibuc.hello.utils.ResponseUtils.*;

@Controller
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private DLCService dlcService;

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<GameEntity> getGameById(@PathVariable String id) {
        return ok(gameService.getGameById(id));
    }

    @GetMapping("/{id}/dlcs")
    @ResponseBody
    public ResponseEntity<List<GameEntity>> getGameDLCs(@PathVariable String id) {
        return ok(gameService.getGameDLCs(id));
    }

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<List<GameEntity>> getGames() {
        return ok(gameService.getAllGames());
    }

    @PostMapping("")
    @ResponseBody
    @DeveloperOnly
    public ResponseEntity<GameEntity> createGame(@RequestBody Game game) {
        return created(gameService.createGame(game));
    }

    @PostMapping("/{id}/addDLC")
    @ResponseBody
    @DeveloperOnly
    public ResponseEntity<GameEntity> addDLC(@PathVariable String id, @RequestBody Game dlc) {
        return created(dlcService.createDLC(id, dlc));
    }

    @PutMapping("/{id}")
    @ResponseBody
    @DeveloperOnly
    public ResponseEntity<GameEntity> updateGame(@PathVariable String id, @RequestBody Game game) {
        return ok(gameService.updateGame(id, game));
    }

    @PutMapping("/{id}/addKeys")
    @ResponseBody
    @DeveloperOnly
    public ResponseEntity<GameEntity> addKeys(@PathVariable String id, @RequestParam("quantity") Integer quantity) {
        return ok(gameService.addKeys(id, quantity));
    }

    @PutMapping("/{id}/markOutOfStock")
    @ResponseBody
    @DeveloperOnly
    public ResponseEntity<GameEntity> markOutOfStock(@PathVariable String id) {
        return ok(gameService.markOutOfStock(id));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @DeveloperOnly
    public ResponseEntity<Void> deleteGame(@PathVariable String id) {
        gameService.deleteGame(id);
        return noContent();
    }

}
