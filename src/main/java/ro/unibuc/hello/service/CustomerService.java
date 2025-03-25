package ro.unibuc.hello.service;

import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Customer;
import ro.unibuc.hello.dto.User;
import ro.unibuc.hello.security.AuthenticationUtils;

import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.utils.ValidationUtils.validate;
import static ro.unibuc.hello.utils.ValidationUtils.validateAndUpdate;

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

    @Override
    protected void updateSpecificFields(Customer userInput, UserEntity user) {
        validateAndUpdate("First name", user.getDetails()::setFirstName, userInput.getFirstName());
        validateAndUpdate("Last name", user.getDetails()::setLastName, userInput.getLastName());
    }

    @CustomerOnly
    public UserEntity updateLoggedUser(Customer customerInput) {
        return super.updateLoggedUser(customerInput, AuthenticationUtils.getUser());
    }

}
