package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.CustomerEntity;
import ro.unibuc.hello.data.repository.CustomerRepository;
import ro.unibuc.hello.dto.CustomerInput;
import ro.unibuc.hello.security.AuthenticationService;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    CustomerRepository customerRepository;

    public CustomerEntity getCustomerById(String id) {
        return (customerRepository.findById(id)).get();
    }

    public List<CustomerEntity> getAllCustomers() {
        return customerRepository.findAll();
    }

    public CustomerEntity updateLoggedCustomer(CustomerInput customerInput) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String userId) {
            // TODO: refactor -> Optional pentru atributele CustomerInput
            Optional<CustomerEntity> customer = customerRepository.findById(userId);
            if (customer.isPresent()) {
                customer.get().setUsername(customerInput.getUsername());
                customer.get().setPassword(AuthenticationService.encryptPassword(customerInput.getPassword()));
                customer.get().setEmail(customerInput.getEmail());
                customer.get().setFirstName(customerInput.getFirstName());
                customer.get().setLastName(customerInput.getLastName());

                return customerRepository.save(customer.get());
            }
        }

        return null;
    }

}
