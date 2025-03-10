package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.LibraryRepository;
import ro.unibuc.hello.data.repository.UserRepository;

@Service
public class LibraryService {

    @Autowired
    protected LibraryRepository libraryRepository;

    @Autowired
    protected UserRepository userRepository;

    public ResponseEntity<?> getLibraryByCustomerId(String customerId) {
        return ResponseEntity.ok(libraryRepository.findGamesByCustomer(userRepository.findByIdAndRole(customerId, UserEntity.Role.CUSTOMER)));
    }

}
