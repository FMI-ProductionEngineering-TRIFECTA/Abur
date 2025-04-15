package ro.unibuc.hello.controller;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.WishlistEntity;
import ro.unibuc.hello.service.WishlistService;

import java.util.List;

import static ro.unibuc.hello.utils.ResponseUtils.*;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("")
    @ResponseBody
    @CustomerOnly
    @Timed(value = "hello.wishlist.time", description = "Time taken to return wishlist")
    @Counted(value = "hello.wishlistcount", description = "Times wishlist was returned")
    public ResponseEntity<List<GameEntity>> getWishlist() {
        return ok(wishlistService.getWishlist());
    }

    @PostMapping("/{gameId}")
    @ResponseBody
    @CustomerOnly
    public ResponseEntity<WishlistEntity> addToWishlist(@PathVariable String gameId) {
        return created(wishlistService.addToWishlist(gameId));
    }

    @PostMapping("/moveToCart/{gameId}")
    @ResponseBody
    @CustomerOnly
    public ResponseEntity<Void> moveToCart(@PathVariable String gameId) {
        wishlistService.moveToCart(gameId);
        return noContent();
    }

    @PostMapping("/moveToCart")
    @ResponseBody
    @CustomerOnly
    public ResponseEntity<Void> moveAllToCart() {
        wishlistService.moveAllToCart();
        return noContent();
    }

    @DeleteMapping("/{gameId}")
    @ResponseBody
    @CustomerOnly
    public ResponseEntity<Void> removeFromWishlist(@PathVariable String gameId) {
        wishlistService.removeFromWishlist(gameId);
        return noContent();
    }

    @DeleteMapping("/clear")
    @CustomerOnly
    @ResponseBody
    public ResponseEntity<Void> removeAllFromWishlist() {
        wishlistService.removeAllFromWishlist();
        return noContent();
    }

}
