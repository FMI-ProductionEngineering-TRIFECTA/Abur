package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.service.CustomerService;

import java.util.List;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<UserEntity> getCustomerById(@PathVariable String id) {
        return customerService.getUserById(id);
    }

    @GetMapping("/{id}/games")
    @ResponseBody
    public ResponseEntity<List<GameEntity>> getCustomerGames(@PathVariable String id) {
        return customerService.getGames(id);
    }

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<List<UserEntity>> getAllCustomers() {
        return customerService.getAllUsers();
    }

    @GetMapping("/myGames")
    @ResponseBody
    public ResponseEntity<List<GameEntity>> getMyGames() {
        return customerService.getGames();
    }

    @PutMapping("")
    @ResponseBody
    public ResponseEntity<UserEntity> updateLoggedCustomer(@RequestBody Customer customer) {
        return customerService.updateLoggedUser(customer);
    }

}
