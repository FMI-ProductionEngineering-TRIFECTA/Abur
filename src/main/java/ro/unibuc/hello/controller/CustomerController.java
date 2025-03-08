package ro.unibuc.hello.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.security.AuthenticationService;
import ro.unibuc.hello.service.CustomerService;

import java.util.List;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    CustomerService customerService;

    @Autowired
    AuthenticationService authenticationService;

    @GetMapping("/{id}")
    @ResponseBody
    public UserEntity getCustomerById(@PathVariable String id) {
        return customerService.getUserById(id);
    }

    @GetMapping("")
    @ResponseBody
    public List<UserEntity> getAllCustomers() {
        return customerService.getAllUsers();
    }

    @PutMapping("")
    @ResponseBody
    public UserEntity updateLoggedCustomer(@Valid @RequestBody Customer customer) {
        if (authenticationService.hasAccess(UserEntity.Role.CUSTOMER)) {
            return customerService.updateLoggedUser(customer);
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied");
    }

}
