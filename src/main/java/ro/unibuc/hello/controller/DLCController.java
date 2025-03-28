package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.annotation.DeveloperOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.service.DLCService;

import java.util.List;

import static ro.unibuc.hello.utils.ResponseUtils.noContent;
import static ro.unibuc.hello.utils.ResponseUtils.ok;

@Controller
@RequestMapping("/dlcs")
public class DLCController {

    @Autowired
    private DLCService dlcService;

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<GameEntity> getDLCyId(@PathVariable String id) {
        return ok(dlcService.getGameById(id));
    }

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<List<GameEntity>> getDLCs() {
        return ok(dlcService.getAllGames());
    }

    @PutMapping("/{id}")
    @ResponseBody
    @DeveloperOnly
    public ResponseEntity<GameEntity> updateGame(@PathVariable String id, @RequestBody Game game) {
        return ok(dlcService.updateGame(id, game));
    }

    @PutMapping("/{id}/addKeys")
    @ResponseBody
    @DeveloperOnly
    public ResponseEntity<GameEntity> addKeys(@PathVariable String id, @RequestParam("quantity") Integer quantity) {
        return ok(dlcService.addKeys(id, quantity));
    }

    @PutMapping("/{id}/markOutOfStock")
    @ResponseBody
    @DeveloperOnly
    public ResponseEntity<GameEntity> markOutOfStock(@PathVariable String id) {
        return ok(dlcService.markOutOfStock(id));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @DeveloperOnly
    public ResponseEntity<Void> deleteGame(@PathVariable String id) {
        dlcService.deleteGame(id);
        return noContent();
    }

}
