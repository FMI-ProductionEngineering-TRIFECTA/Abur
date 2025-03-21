package ro.unibuc.hello.service;

import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.User;
import ro.unibuc.hello.security.AuthenticationUtils;

import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.utils.ValidationUtils.validate;

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

    public UserEntity getCustomer(String customerId) {
        return getUser(customerId);
    }

    @CustomerOnly
    public UserEntity updateLoggedUser(Customer customerInput) {
        return super.updateLoggedUser(customerInput, AuthenticationUtils.getUser());
    }

}
