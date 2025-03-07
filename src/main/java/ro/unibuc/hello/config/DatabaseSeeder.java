package ro.unibuc.hello.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ro.unibuc.hello.data.entity.InformationEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.InformationRepository;
import ro.unibuc.hello.data.repository.UserRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class DatabaseSeeder {
    @Autowired
    private InformationRepository informationRepository;

    @Autowired
    private UserRepository userRepository;

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
    protected  CompletableFuture<Void> seedUser() {
        return seedEntity(userRepository, List.of(
                // Developers
                new UserEntity(
                        "67c9f02a5582625f6c6639b4",
                        "PlayStationStudios",
                        "PlayStationStudios1234",
                        "contact@sony.com",
                        UserEntity.Role.DEVELOPER,
                        UserEntity.UserDetails.forDeveloper("PlayStation Studios", "https://www.playstation.com/playstation-studios/")
                ),

                // Customers
                new UserEntity(
                        "67c9f02a5582625f6c6639b5",
                        "FixBambucea",
                        "FixBambucea1234",
                        "fixbambucea@gmail.com",
                        UserEntity.Role.CUSTOMER,
                        UserEntity.UserDetails.forCustomer("Fix", "Bambucea")
                )
        ));
    }

    @PostConstruct
    public void seedData() {
        List<CompletableFuture<Void>> futures = List.of(
                seedInformation(),
                seedUser()
        );

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
