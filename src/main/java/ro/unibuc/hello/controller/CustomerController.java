package ro.unibuc.hello.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.security.AuthenticationUtils;
import ro.unibuc.hello.service.CustomerService;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getCustomerById(@PathVariable String id) {
        return customerService.getUserById(id);
    }

    @GetMapping("/{id}/games")
    @ResponseBody
    public ResponseEntity<?> getCustomerGames(@PathVariable String id) {
        return customerService.getGames(id);
    }

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<?> getAllCustomers() {
        return customerService.getAllUsers();
    }

    @GetMapping("/myGames")
    @ResponseBody
    public ResponseEntity<?> getMyGames() {
        return customerService.getGames();
    }

    @PutMapping("")
    @ResponseBody
    public ResponseEntity<?> updateLoggedCustomer(@RequestBody Customer customer) {
        return customerService.updateLoggedUser(customer);
    }

}
