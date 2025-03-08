package ro.unibuc.hello.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.InformationEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.data.repository.InformationRepository;
import ro.unibuc.hello.data.repository.UserRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static ro.unibuc.hello.data.entity.GameEntity.*;
import static ro.unibuc.hello.data.entity.UserEntity.*;

@Component
public class DatabaseSeeder {

    @Autowired
    private InformationRepository informationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Async
    protected void seedInformation() {
        informationRepository.saveAll(List.of(
                new InformationEntity(
                        "Overview",
                        "This is an example of using a data storage engine running separately from our applications server"
                )
        ));
    }

    @Async
    protected void seedDeveloper() {
        userRepository.saveAll(List.of(
                buildDeveloper(
                        "67c9f02a5582625f6c6639u1",
                        "contact@sony.com",
                        "PlayStation Studios",
                        "https://www.playstation.com/playstation-studios/"
                )
        ));
    }

    @Async
    protected void seedCustomer() {
        userRepository.saveAll(List.of(
                buildCustomer(
                        "67c9f02a5582625f6c6639u2",
                        "fixbambucea@gmail.com",
                        "Fix",
                        "Bambucea"
                )
        ));
    }

    @Async
    protected void seedGame() {
        gameRepository.saveAll(List.of(
                buildGame(
                        "67c9f02a5582625f6c6639g1",
                        "Horizon Zero Dawn",
                        59.99,
                        "18-02-2017",
                        userRepository.findByIdAndRole("67c9f02a5582625f6c6639u1", UserEntity.Role.DEVELOPER)
                )
        ));
    }

    @Async
    protected void seedDLC() {
        gameRepository.saveAll(List.of(
                buildDLC(
                        "67c9f02a5582625f6c6639g2",
                        "Horizon Zero Dawn: The Frozen Wilds",
                        9.99,
                        "07-09-2017",
                        userRepository.findByIdAndRole("67c9f02a5582625f6c6639u1", UserEntity.Role.DEVELOPER),
                        gameRepository.findByIdAndType("67c9f02a5582625f6c6639g1", GameEntity.Type.GAME)
                )
        ));
    }

    private CompletableFuture<Void> executeAsync(List<Runnable> actions) {
        return CompletableFuture.allOf(actions
                .stream()
                .map(CompletableFuture::runAsync)
                .toArray(CompletableFuture[]::new)
        );
    }

    @PostConstruct
    public void seedData() {
        executeAsync(List.of(
            informationRepository::deleteAll,
            userRepository::deleteAll,
            gameRepository::deleteAll
        ))
        .thenCompose(ignored -> executeAsync(List.of(
            this::seedInformation,
            this::seedDeveloper,
            this::seedCustomer
        )))
        .thenRunAsync(this::seedGame)
        .thenRunAsync(this::seedDLC)
        .join();
    }

}
