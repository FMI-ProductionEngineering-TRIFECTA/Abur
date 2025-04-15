package ro.unibuc.hello.controller;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.CartEntity;
import ro.unibuc.hello.dto.CartInfo;
import ro.unibuc.hello.service.CartService;

import static ro.unibuc.hello.utils.ResponseUtils.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("")
    @ResponseBody
    @CustomerOnly
    @Timed(value = "hello.cart.time", description = "Time taken to return cart")
    @Counted(value = "hello.cart.count", description = "Times cart was returned")
    public ResponseEntity<CartInfo> getCart() {
        return ok(cartService.getCart());
    }

    @PostMapping("/checkout")
    @ResponseBody
    @CustomerOnly
    @Timed(value = "hello.checkout.time", description = "Time taken to return checkout")
    @Counted(value = "hello.checkout.count", description = "Times checkout was returned")
    public ResponseEntity<Void> checkout() {
        cartService.checkout();
        return noContent();
    }

    @PostMapping("/{gameId}")
    @ResponseBody
    @CustomerOnly
    public ResponseEntity<CartEntity> addToCart(@PathVariable String gameId) {
        return created(cartService.addToCart(gameId));
    }

    @DeleteMapping("/{gameId}")
    @ResponseBody
    @CustomerOnly
    public ResponseEntity<Void> removeFromCart(@PathVariable String gameId) {
        cartService.removeFromCart(gameId);
        return noContent();
    }

    @DeleteMapping("/clear")
    @ResponseBody
    @CustomerOnly
    public ResponseEntity<Void> removeAllFromCart() {
        cartService.removeAllFromCart();
        return noContent();
    }

}
