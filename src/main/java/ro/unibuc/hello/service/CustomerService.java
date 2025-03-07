package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.CustomerInput;

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

    public UserEntity updateLoggedCustomer(CustomerInput customerInput) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String userId) {
            // TODO: refactor -> Optional pentru atributele CustomerInput
            Optional<UserEntity> customer = userRepository.findById(userId);
            if (customer.isPresent()) {
                customer.get().setUsername(customerInput.getUsername());
                customer.get().setPassword(customerInput.getPassword());
                customer.get().setEmail(customerInput.getEmail());
                customer.get().setDetails(UserEntity.UserDetails.forCustomer(
                        customerInput.getFirstName(),
                        customerInput.getLastName()
                ));

                return userRepository.save(customer.get());
            }
        }

        return null;
    }

}
