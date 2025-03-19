package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.WishlistEntity;
import ro.unibuc.hello.service.WishlistService;

import java.util.List;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<List<GameEntity>> getWishlist() {
        return wishlistService.getWishlist();
    }

    @PostMapping("/{gameId}")
    @ResponseBody
    public ResponseEntity<WishlistEntity> addToWishlist(@PathVariable String gameId) {
        return wishlistService.addToWishlist(gameId);
    }

    @PostMapping("/moveToCart/{gameId}")
    @ResponseBody
    public ResponseEntity<Void> moveToCart(@PathVariable String gameId) {
        return wishlistService.moveToCart(gameId);
    }

    @PostMapping("/moveToCart")
    @ResponseBody
    public ResponseEntity<Void> moveAllToCart() {
        return wishlistService.moveAllToCart();
    }

    @DeleteMapping("/{gameId}")
    @ResponseBody
    public ResponseEntity<Void> removeFromWishlist(@PathVariable String gameId) {
        return wishlistService.removeFromWishlist(gameId);
    }

    @DeleteMapping("/clear")
    @ResponseBody
    public ResponseEntity<Void> removeAllFromWishlist() {
        return wishlistService.removeAllFromWishlist();
    }

}
