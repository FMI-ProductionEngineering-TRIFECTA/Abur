package ro.unibuc.hello.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.repository.LibraryRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static ro.unibuc.hello.security.AuthenticationUtils.getUser;

@Service
public class LibraryService {

    @Autowired
    protected LibraryRepository libraryRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MeterRegistry metricsRegistry;

    private final AtomicLong counter = new AtomicLong();

    public List<GameEntity> getLibraryByCustomerId(String customerId) {
        metricsRegistry
                .counter("my_non_aop_metric", "endpoint", "library")
                .increment(counter.incrementAndGet());

        return libraryRepository.getGamesByCustomer(customerService.getCustomer(customerId));
    }

    @CustomerOnly
    public List<GameEntity> getLibrary() {
        return getLibraryByCustomerId(getUser().getId());
    }

}
