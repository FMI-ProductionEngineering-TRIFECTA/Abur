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
import ro.unibuc.hello.utils.SeederUtils;

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
                        "PlayStation Studios",
                        "https://www.playstation.com/playstation-studios/"
                ),
                buildDeveloper(
                        "CD PROJEKT RED",
                        "https://www.cdprojektred.com/en"
                ),
                buildDeveloper(
                        "Square Enix",
                        "https://www.square-enix.com/"
                ),
                buildDeveloper(
                        "Nintendo",
                        "https://www.nintendo.com/us/"
                ),
                buildDeveloper(
                        "18Light Game",
                        "https://www.18light.cc/en/"
                ),
                buildDeveloper(
                        "Capcom",
                        "https://www.capcom.com/"
                ),
                buildDeveloper(
                        "FromSoftware",
                        "https://www.fromsoftware.jp/ww/"
                ),
                buildDeveloper(
                        "Kojima Productions",
                        "https://www.kojimaproductions.jp/en"
                ),
                buildDeveloper(
                        "Rockstar Games",
                        "https://www.fromsoftware.jp/ww/"
                ),
                buildDeveloper(
                        "Frictional Games",
                        "https://frictionalgames.com/"
                )
        ));
    }

    @Async
    protected void seedCustomer() {
        userRepository.saveAll(List.of(
                buildCustomer(
                        "Bambucea",
                        "Fix"
                ),
                buildCustomer(
                        "Bradea",
                        "Codrin"
                ),
                buildCustomer(
                        "Andrei",
                        "Neculae"
                ),
                buildCustomer(
                        "Sebi",
                        "Mihalache"
                ),
                buildCustomer(
                        "Giulian",
                        "Buzatu"
                )
        ));
    }

    @Async
    protected void seedGame() {
        gameRepository.saveAll(List.of(
                buildGame(
                        "Horizon Zero Dawn",
                        59.99,
                        "18-02-2017",
                        userRepository.findByIdAndRole(SeederUtils.getId("developers", 0), UserEntity.Role.DEVELOPER)
                )
        ))
        .forEach(gameEntity -> {
            UserEntity developer = gameEntity.getDeveloper();
            developer.getGames().add(gameEntity);
            userRepository.save(developer);
        });
    }

    @Async
    protected void seedDLC() {
        gameRepository.saveAll(List.of(
                buildDLC(
                        "Horizon Zero Dawn: The Frozen Wilds",
                        9.99,
                        "07-09-2017",
                        userRepository.findByIdAndRole(SeederUtils.getId("developers", 0), UserEntity.Role.DEVELOPER),
                        gameRepository.findByIdAndType(SeederUtils.getId("games", 0), GameEntity.Type.GAME)
                )
        ))
        .forEach(dlcEntity -> {
            UserEntity developer = dlcEntity.getDeveloper();
            developer.getGames().add(dlcEntity);
            userRepository.save(developer);

            GameEntity baseGame = dlcEntity.getBaseGame();
            baseGame.getDlcs().add(dlcEntity);
            gameRepository.save(baseGame);
        });
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
        SeederUtils.setTemplate("developers", "67c9f02a5582625f6c6639dev");
        SeederUtils.setTemplate("customers", "67c9f02a5582625f6c6639cust");
        SeederUtils.setTemplate("games", "67c9f02a5582625f6c6639game");
        SeederUtils.setTemplate("dlcs", "67c9f02a5582625f6c6639dlc");

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
