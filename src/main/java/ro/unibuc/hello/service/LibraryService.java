package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.repository.LibraryRepository;

import java.util.List;

import static ro.unibuc.hello.utils.ResponseUtils.*;
import static ro.unibuc.hello.security.AuthenticationUtils.*;

@Service
public class LibraryService {

    @Autowired
    protected LibraryRepository libraryRepository;

    @Autowired
    private CustomerService customerService;

    public ResponseEntity<List<GameEntity>> getLibraryByCustomerId(String customerId) {
        return ok(libraryRepository.getGamesByCustomer(customerService.getCustomer(customerId)));
    }

    @CustomerOnly
    public ResponseEntity<List<GameEntity>> getLibrary() {
        return getLibraryByCustomerId(getUser().getId());
    }

}
