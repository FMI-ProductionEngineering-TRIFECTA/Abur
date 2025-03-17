package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.repository.LibraryRepository;
import ro.unibuc.hello.data.repository.UserRepository;

import static ro.unibuc.hello.utils.ResponseUtils.*;
import static ro.unibuc.hello.security.AuthenticationUtils.*;

@Service
public class LibraryService {

    @Autowired
    protected LibraryRepository libraryRepository;

    @Autowired
    protected UserRepository userRepository;

    public ResponseEntity<?> getLibraryByCustomerId(String customerId) {
        return ok(libraryRepository.getGamesByCustomer(userRepository.getCustomer(customerId)));
    }

    @CustomerOnly
    public ResponseEntity<?> getLibrary() {
        return getLibraryByCustomerId(getUser().getId());
    }

}
