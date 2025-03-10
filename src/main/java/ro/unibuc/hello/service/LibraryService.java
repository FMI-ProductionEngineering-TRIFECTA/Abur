package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import ro.unibuc.hello.data.repository.LibraryRepository;
import ro.unibuc.hello.data.repository.UserRepository;

import static org.springframework.http.ResponseEntity.ok;
import static ro.unibuc.hello.data.entity.UserEntity.Role;

@Service
public class LibraryService {

    @Autowired
    protected LibraryRepository libraryRepository;

    @Autowired
    protected UserRepository userRepository;

    public ResponseEntity<?> getLibraryByCustomerId(String customerId) {
        return ok(libraryRepository.findGamesByCustomer(userRepository.findByIdAndRole(customerId, Role.CUSTOMER)));
    }

}
