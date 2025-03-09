package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.service.DLCService;

@Controller
@RequestMapping("/dlcs")
public class DLCController {

    @Autowired
    private DLCService dlcService;

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getDLCyId(@PathVariable String id) {
        return dlcService.getGameById(id);
    }

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<?> getDLCs() {
        return dlcService.getAllGames();
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateGame(@PathVariable String id, @RequestBody Game game) {
        return dlcService.updateGame(id, game);
    }

    @PutMapping("/{id}/addKeys")
    @ResponseBody
    public ResponseEntity<?> addKeys(@PathVariable String id, @RequestParam("quantity") Integer quantity) {
        return dlcService.addKeys(id, quantity);
    }

    @PutMapping("/{id}/markOutOfStock")
    @ResponseBody
    public ResponseEntity<?> markOutOfStock(@PathVariable String id) {
        return dlcService.markOutOfStock(id);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteGame(@PathVariable String id) {
        return dlcService.deleteGame(id);
    }

}
