package ro.unibuc.hello.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ro.unibuc.hello.data.entity.CustomerEntity;
import ro.unibuc.hello.data.entity.DeveloperEntity;
import ro.unibuc.hello.data.entity.InformationEntity;
import ro.unibuc.hello.data.repository.CustomerRepository;
import ro.unibuc.hello.data.repository.DeveloperRepository;
import ro.unibuc.hello.data.repository.InformationRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class DatabaseSeeder {
    @Autowired
    private InformationRepository informationRepository;

    @Autowired
    private DeveloperRepository developerRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private <T> CompletableFuture<Void> seedEntity(MongoRepository<T, String> entityRepository, List<T> entities) {
        entityRepository.deleteAll();
        entityRepository.saveAll(entities);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    protected CompletableFuture<Void> seedInformation() {
        return seedEntity(informationRepository, List.of(
                new InformationEntity(
                        "Overview",
                        "This is an example of using a data storage engine running separately from our applications server"
                )
        ));
    }

    @Async
    protected CompletableFuture<Void> seedDeveloper() {
        return seedEntity(developerRepository, List.of(
                new DeveloperEntity(
                        "67c9f02a5582625f6c6639b4",
                        "PlayStationStudios",
                        "PlayStationStudios1234",
                        "contact@sony.com",
                        "PlayStation Studios",
                        "https://www.playstation.com/playstation-studios/"
                ),
                new DeveloperEntity(
                        "67c9f02a5582625f6c6639b5",
                        "XboxGameStudios",
                        "XboxGameStudios1234",
                        "contact@microsoft.com",
                        "Xbox Game Studios",
                        "https://www.xbox.com/xbox-game-studios/"
                )
        ));
    }

    @Async
    protected CompletableFuture<Void> seedCustomer() {
        return seedEntity(customerRepository, List.of(
                new CustomerEntity(
                        "67c9f02a5582625f6c6639b6",
                        "FixBambucea",
                        "FixBambucea1234",
                        "fixbambucea@gmail.com",
                        "Bambucea",
                        "Fix"
                )
        ));
    }

    @PostConstruct
    public void seedData() {
        List<CompletableFuture<Void>> futures = List.of(
                seedInformation(),
                seedDeveloper(),
                seedCustomer()
        );

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
