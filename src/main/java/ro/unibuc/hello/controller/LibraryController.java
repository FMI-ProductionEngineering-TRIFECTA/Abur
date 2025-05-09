package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.service.LibraryService;

import java.util.List;

import static ro.unibuc.hello.utils.ResponseUtils.ok;

@Controller
@RequestMapping("/library")
public class LibraryController {

    @Autowired
    private LibraryService libraryService;

    @GetMapping("")
    @ResponseBody
    @CustomerOnly
    public ResponseEntity<List<GameEntity>> getLibrary() {
        return ok(libraryService.getLibrary());
    }

    @GetMapping("/{customerId}")
    @ResponseBody
    public ResponseEntity<List<GameEntity>> getLibraryByCustomerId(@PathVariable String customerId) {
        return ok(libraryService.getLibraryByCustomerId(customerId));
    }

}
