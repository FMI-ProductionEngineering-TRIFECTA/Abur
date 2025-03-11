package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.LibraryRepository;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.exception.NotFoundException;

import static org.springframework.http.ResponseEntity.ok;
import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.security.AuthenticationUtils.*;

@Service
public class LibraryService {

    @Autowired
    protected LibraryRepository libraryRepository;

    @Autowired
    protected UserRepository userRepository;

    public ResponseEntity<?> getLibraryByCustomerId(String customerId) {
        UserEntity customer = userRepository.findByIdAndRole(customerId, Role.CUSTOMER);
        if (customer == null) throw new NotFoundException("No customer found at id %s", customerId);

        return ok(libraryRepository.getGamesByCustomer(userRepository.findByIdAndRole(customerId, Role.CUSTOMER)));
    }

    @CustomerOnly
    public ResponseEntity<?> getLibrary() {
        return getLibraryByCustomerId(getUser().getId());
    }

}
