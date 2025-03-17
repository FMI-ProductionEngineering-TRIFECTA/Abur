package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.service.CartService;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<?> getCart() {
        return cartService.getCart();
    }

    @PostMapping("/checkout")
    @ResponseBody
    public ResponseEntity<?> checkout() {
        return cartService.checkout();
    }

    @PostMapping("/{gameId}")
    @ResponseBody
    public ResponseEntity<?> addToCart(@PathVariable String gameId) {
        return cartService.addGameToCartById(gameId);
    }

    @DeleteMapping("/{gameId}")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@PathVariable String gameId) {
        return cartService.removeGameFromCart(gameId);
    }

    @DeleteMapping("/clear")
    @ResponseBody
    public ResponseEntity<?> removeAllFromCart() {
        return cartService.removeAllFromCart();
    }

}
