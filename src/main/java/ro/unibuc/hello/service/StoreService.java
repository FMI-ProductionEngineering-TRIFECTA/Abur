package ro.unibuc.hello.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.data.repository.LibraryRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static ro.unibuc.hello.security.AuthenticationUtils.getUser;

@Service
public class StoreService {

    @Autowired
    protected GameRepository gameRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private MeterRegistry metricsRegistry;

    @CustomerOnly
    public List<GameEntity> getStore(Boolean hideOwned) {
        metricsRegistry
                .counter("abur_view_metric", "endpoint", "store")
                .increment(1);

        List<GameEntity> gamesAndDlcs = gameRepository.findAll();
        if (hideOwned) gamesAndDlcs.removeAll(libraryRepository.getGamesByCustomer(getUser()));

        return gamesAndDlcs;
    }

}
