package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.service.StoreService;

import java.util.List;

@Controller
@RequestMapping("/store")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<List<GameEntity>> getStore(@RequestParam Boolean hideOwned) {
        return storeService.getStore(hideOwned);
    }

}
