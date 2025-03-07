package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Customer;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    UserRepository userRepository;

    public UserEntity getCustomerById(String id) {
        return userRepository.findByIdAndRole(id, UserEntity.Role.CUSTOMER);
    }

    public List<UserEntity> getAllCustomers() {
        return userRepository.findByRole(UserEntity.Role.CUSTOMER);
    }

    public UserEntity updateLoggedCustomer(Customer customerInput) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String userId) {
            Optional<UserEntity> customer = userRepository.findById(userId);
            if (customer.isPresent()) {
                if (customerInput.getUsername() != null && !customerInput.getUsername().isBlank()) {
                    customer.get().setUsername(customerInput.getUsername());
                }
                if (customerInput.getPassword() != null && !customerInput.getPassword().isBlank()) {
                    customer.get().setPassword(customerInput.getPassword());
                }
                if (customerInput.getEmail() != null && !customerInput.getEmail().isBlank()) {
                    customer.get().setEmail(customerInput.getEmail());
                }
                if (customerInput.getFirstName() != null && !customerInput.getFirstName().isBlank()) {
                    customer.get().getDetails().setFirstName(customerInput.getFirstName());
                }
                if (customerInput.getLastName() != null && !customerInput.getLastName().isBlank()) {
                    customer.get().getDetails().setLastName(customerInput.getLastName());
                }

                return userRepository.save(customer.get());
            }
        }

        return null;
    }

}
