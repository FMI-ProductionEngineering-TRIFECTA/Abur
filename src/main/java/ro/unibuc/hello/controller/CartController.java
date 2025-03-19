package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.data.entity.CartEntity;
import ro.unibuc.hello.dto.CartInfo;
import ro.unibuc.hello.service.CartService;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<CartInfo> getCart() {
        return cartService.getCart();
    }

    @PostMapping("/checkout")
    @ResponseBody
    public ResponseEntity<Void> checkout() {
        return cartService.checkout();
    }

    @PostMapping("/{gameId}")
    @ResponseBody
    public ResponseEntity<CartEntity> addToCart(@PathVariable String gameId) {
        return cartService.addToCart(gameId);
    }

    @DeleteMapping("/{gameId}")
    @ResponseBody
    public ResponseEntity<Void> removeFromCart(@PathVariable String gameId) {
        return cartService.removeFromCart(gameId);
    }

    @DeleteMapping("/clear")
    @ResponseBody
    public ResponseEntity<Void> removeAllFromCart() {
        return cartService.removeAllFromCart();
    }

}
