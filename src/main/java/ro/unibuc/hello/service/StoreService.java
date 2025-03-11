package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.repository.GameRepository;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@Service
public class StoreService {

    @Autowired
    protected GameRepository gameRepository;

    @CustomerOnly
    public ResponseEntity<?> getStore() {
        List<GameEntity> gamesAndDlcs = gameRepository.findAll();

        return ok(gamesAndDlcs);
    }
}
