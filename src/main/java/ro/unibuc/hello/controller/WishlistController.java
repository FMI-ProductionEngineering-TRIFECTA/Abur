package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import ro.unibuc.hello.service.WishlistService;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<?> getWishlist() {
        return wishlistService.getWishlist();
    }

    @PostMapping("/{gameId}")
    @ResponseBody
    public ResponseEntity<?> addToWishlist(@PathVariable String gameId) {
        return wishlistService.addToWishlist(gameId);
    }

    @PostMapping("/moveToCart/{gameId}")
    @ResponseBody
    public ResponseEntity<?> moveToCart(@PathVariable String gameId) {
        return wishlistService.moveToCart(gameId);
    }

    @PostMapping("/moveToCart")
    @ResponseBody
    public ResponseEntity<?> moveAllToCart() {
        return wishlistService.moveAllToCart();
    }

    @DeleteMapping("/{gameId}")
    @ResponseBody
    public ResponseEntity<?> removeFromWishlist(@PathVariable String gameId) {
        return wishlistService.removeFromWishlist(gameId);
    }

    @DeleteMapping("/clear")
    @ResponseBody
    public ResponseEntity<?> removeAllFromWishlist() {
        return wishlistService.removeAllFromWishlist();
    }
}
