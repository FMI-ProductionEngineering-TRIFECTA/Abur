package ro.unibuc.hello.service;

import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Customer;

@Service
public class CustomerService extends UserService<Customer> {

    @Override
    protected UserEntity.Role getRole() {
        return UserEntity.Role.CUSTOMER;
    }

}
