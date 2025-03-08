package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.service.GameService;

import java.util.List;

@Controller
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameService gameService;

    @GetMapping("/{id}")
    @ResponseBody
    public GameEntity getGameById(@PathVariable String id) {
        return gameService.getGameById(id);
    }

    @GetMapping("")
    @ResponseBody
    public List<GameEntity> getGames() {
        return gameService.getAllGames();
    }

}
