package ro.unibuc.hello.service;

import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.User;

import static ro.unibuc.hello.utils.ValidationUtils.*;

@Service
public class CustomerService extends UserService<Customer> {

    @Override
    protected UserEntity.Role getRole() {
        return UserEntity.Role.CUSTOMER;
    }

    @Override
    protected void validateDetails(User user) {
        Customer customer = (Customer) user;

        validate("First name", customer.getFirstName());
        validate("Last name", customer.getLastName());
    }

}
