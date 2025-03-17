package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.data.repository.LibraryRepository;

import java.util.List;

import static ro.unibuc.hello.utils.ResponseUtils.*;
import static ro.unibuc.hello.security.AuthenticationUtils.getUser;

@Service
public class StoreService {

    @Autowired
    protected GameRepository gameRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @CustomerOnly
    public ResponseEntity<?> getStore(Boolean hideOwned) {
        List<GameEntity> gamesAndDlcs = gameRepository.findAll();
        if (hideOwned) gamesAndDlcs.removeAll(libraryRepository.getGamesByCustomer(getUser()));

        return ok(gamesAndDlcs);
    }

}
