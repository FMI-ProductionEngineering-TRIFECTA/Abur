package ro.unibuc.hello.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.LibraryEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static ro.unibuc.hello.data.entity.CartEntity.buildCartEntry;
import static ro.unibuc.hello.data.entity.GameEntity.*;
import static ro.unibuc.hello.data.entity.LibraryEntity.buildLibraryEntry;
import static ro.unibuc.hello.data.entity.UserEntity.*;
import static ro.unibuc.hello.data.entity.WishlistEntity.buildWishlistEntry;
import static ro.unibuc.hello.utils.DatabaseUtils.getId;
import static ro.unibuc.hello.utils.DatabaseUtils.setTemplate;

@Component
public class DatabaseSeeder {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    private UserEntity getDeveloper(Integer id) {
        return userRepository.findByIdAndRole(getId("developers", id), Role.DEVELOPER);
    }

    private UserEntity getCustomer(Integer id) {
        return userRepository.findByIdAndRole(getId("customers", id), Role.CUSTOMER);
    }

    private GameEntity getGame(Integer id) {
        return gameRepository.findByIdAndType(getId("games", id), Type.GAME);
    }

    private void updateDeveloper(GameEntity gameEntity) {
        UserEntity developer = userRepository.findByIdAndRole(gameEntity.getDeveloper().getId(), Role.DEVELOPER);
        developer.getGames().add(gameEntity);
        userRepository.save(developer);
    }

    private void updateCustomer(LibraryEntity libraryEntity) {
        UserEntity customer = userRepository.findByIdAndRole(libraryEntity.getCustomer().getId(), Role.CUSTOMER);
        customer.getGames().add(libraryEntity.getGame());
        userRepository.save(customer);
    }

    private void updateBaseGame(GameEntity dlcEntity) {
        GameEntity baseGame = gameRepository.findByIdAndType(dlcEntity.getBaseGame().getId(), Type.GAME);
        baseGame.getDlcs().add(dlcEntity);
        gameRepository.save(baseGame);
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
                    "Fix",
                    "Bambucea"
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
                    "God of War Ragnarok",
                    59.99,
                    "19-09-2024",
                    getDeveloper(0)
            ),
            buildGame(
                    "Cyberpunk 2077",
                    59.99,
                    "10-12-2020",
                    getDeveloper(1)
            ),
            buildGame(
                    "Life is Strange 2",
                    31.96,
                    "26-09-2018",
                    getDeveloper(2)
            ),
            buildGame(
                    "The Legend of Zelda: Breath of the Wild",
                    59.99,
                    "03-03-2017",
                    getDeveloper(3)
            ),
            buildGame(
                    "Pronty",
                    14.99,
                    "19-11-2021",
                    getDeveloper(4)
            ),
            buildGame(
                    "Devil May Cry 5",
                    29.99,
                    "24-04-2023",
                    getDeveloper(5)
            ),
            buildGame(
                    "Elden Ring",
                    59.99,
                    "25-02-2022",
                    getDeveloper(6)
            ),
            buildGame(
                    "Death Stranding",
                    9.99,
                    "29-03-2022",
                    getDeveloper(7)
            ),
            buildGame(
                    "GTA VI",
                    129.99,
                    "04-10-2030",
                    getDeveloper(8)
            ),
            buildGame(
                    "Soma",
                    28.99,
                    "22-09-2015",
                    getDeveloper(9)
            )
        ))
        .forEach(this::updateDeveloper);
    }

    @Async
    protected void seedDLC() {
        gameRepository.saveAll(List.of(
            buildDLC(
                    "God Of War Ragnarok Valhalla",
                    0.0,
                    "20-08-2024",
                    getDeveloper(0),
                    getGame(0)
            ),
            buildDLC(
                    "Cyberpunk 2077 Phantom Liberty",
                    29.99,
                    "26-09-2023",
                    getDeveloper(1),
                    getGame(1)
            ),
            buildDLC(
                    "Life is Strange 2 Mascot Bundle",
                    1.99,
                    "27-09-2018",
                    getDeveloper(2),
                    getGame(2)
            ),
            buildDLC(
                    "The Legend of Zelda: Breath of the Wild Expansion Pass",
                    19.99,
                    "30-06-2017",
                    getDeveloper(3),
                    getGame(3)
            ),
            buildDLC(
                    "Pronty: Neptune’s Hall",
                    0.0,
                    "24-01-2022",
                    getDeveloper(4),
                    getGame(4)
            ),
            buildDLC(
                    "Devil May Cry 5: Playable Character Vergil",
                    4.95,
                    "15-12-2020",
                    getDeveloper(5),
                    getGame(5)
            ),
            buildDLC(
                    "Elden Ring: Shadow of the Erdtree",
                    39.99,
                    "21-06-2024",
                    getDeveloper(6),
                    getGame(6)
            ),
            buildDLC(
                    "Death Stranding Director's Cut",
                    9.99,
                    "24-01-2022",
                    getDeveloper(7),
                    getGame(7)
            ),
            buildDLC(
                    "GTA VI 1000000 Cash",
                    0.0,
                    "24-01-2035",
                    getDeveloper(8),
                    getGame(8)
            ),
            buildDLC(
                    "Soma Safe Mode",
                    0.0,
                    "22-09-2015",
                    getDeveloper(9),
                    getGame(9)
            )
        ))
        .forEach(dlcEntity -> {
            updateBaseGame(dlcEntity);
            updateDeveloper(dlcEntity);
        });
    }

    protected void seedLibrary() {
        libraryRepository.saveAll(List.of(
            buildLibraryEntry(
                    getGame(0),
                    getCustomer(0)
            ),
            buildLibraryEntry(
                    getGame(1),
                    getCustomer(0)
            ),
            buildLibraryEntry(
                    getGame(2),
                    getCustomer(0)
            ),
            buildLibraryEntry(
                    getGame(0),
                    getCustomer(1)
            )
        ))
        .forEach(this::updateCustomer);
    }

    protected void seedCart() {
        cartRepository.saveAll(List.of(
            buildCartEntry(
                    getGame(3),
                    getCustomer(0)
            ),
            buildCartEntry(
                    getGame(4),
                    getCustomer(0)
            ),
            buildCartEntry(
                    getGame(5),
                    getCustomer(0)
            ),
            buildCartEntry(
                    getGame(3),
                    getCustomer(1)
            )
        ));
    }

    protected void seedWishlist() {
        wishlistRepository.saveAll(List.of(
            buildWishlistEntry(
                    getGame(6),
                    getCustomer(0)
            ),
            buildWishlistEntry(
                    getGame(7),
                    getCustomer(0)
            ),
            buildWishlistEntry(
                    getGame(8),
                    getCustomer(0)
            ),
            buildWishlistEntry(
                    getGame(9),
                    getCustomer(1)
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
        setTemplate("developers", "67c9f02a5582625f6c6639dev");
        setTemplate("customers", "67c9f02a5582625f6c6639cust");
        setTemplate("games", "67c9f02a5582625f6c6639game");
        setTemplate("dlcs", "67c9f02a5582625f6c6639dlc");

        executeAsync(List.of(
            userRepository::deleteAll,
            gameRepository::deleteAll,
            libraryRepository::deleteAll,
            cartRepository::deleteAll,
            wishlistRepository::deleteAll
        ))
        .thenCompose(ignored -> executeAsync(List.of(
            this::seedDeveloper,
            this::seedCustomer
        )))
        .thenRunAsync(this::seedGame)
        .thenRunAsync(this::seedDLC)
        .thenCompose(ignored -> executeAsync(List.of(
            this::seedLibrary,
            this::seedCart,
            this::seedWishlist
        )))
        .join();
    }

}
