package ro.unibuc.hello.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.User;

import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.security.AuthenticationUtils.*;
import static ro.unibuc.hello.utils.ValidationUtils.*;

@Service
public class CustomerService extends UserService<Customer> {

    @Override
    protected Role getRole() {
        return Role.CUSTOMER;
    }

    @Override
    protected void validateDetails(User user) {
        Customer customer = (Customer) user;

        validate("First name", customer.getFirstName());
        validate("Last name", customer.getLastName());
    }

    @CustomerOnly
    public ResponseEntity<?> updateLoggedUser(Customer customerInput) {
        return super.updateLoggedUser(customerInput, getUser());
    }

}
