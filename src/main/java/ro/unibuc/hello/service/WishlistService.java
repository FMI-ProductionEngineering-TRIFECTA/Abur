package ro.unibuc.hello.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.entity.WishlistEntity;
import ro.unibuc.hello.data.repository.CartRepository;
import ro.unibuc.hello.data.repository.LibraryRepository;
import ro.unibuc.hello.data.repository.WishlistRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static ro.unibuc.hello.data.entity.CartEntity.buildCartEntry;
import static ro.unibuc.hello.data.entity.GameEntity.*;
import static ro.unibuc.hello.data.entity.WishlistEntity.buildWishlistEntry;
import static ro.unibuc.hello.security.AuthenticationUtils.getUser;
import static ro.unibuc.hello.utils.ValidationUtils.validate;

@Service
public class WishlistService {

    @Autowired
    protected WishlistRepository wishlistRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private MeterRegistry metricsRegistry;

    private final AtomicLong counter = new AtomicLong();

    @CustomerOnly
    public List<GameEntity> getWishlist() {
        metricsRegistry
                .counter("my_non_aop_metric", "endpoint", "wishlist")
                .increment(counter.incrementAndGet());

        return wishlistRepository.getGamesByCustomer(getUser());
    }

    @CustomerOnly
    public WishlistEntity addToWishlist(String gameId) {
        UserEntity customer = getUser();
        GameEntity game = gameService.getGame(gameId);

        validate(
                game.getTitle(),
                gameId,
                notInWishlist(wishlistRepository.getGamesByCustomer(customer))
                .and(notInLibrary(libraryRepository.getGamesByCustomer(customer)))
        );

        return wishlistRepository.save(
            buildWishlistEntry(
                    game,
                    customer
            )
        );
    }

    @CustomerOnly
    public void moveToCart(String gameId) {
        UserEntity customer = getUser();
        GameEntity game = gameService.getGame(gameId);

        validate(
                game.getTitle(),
                gameId,
                inWishlist(wishlistRepository.getGamesByCustomer(customer))
                .and(notInCart(cartRepository.getGamesByCustomer(customer)))
        );

        cartRepository.save(
            buildCartEntry(
                game,
                customer
            )
        );
    }

    @CustomerOnly
    public void moveAllToCart() {
        UserEntity customer = getUser();
        wishlistRepository.getGamesByCustomer(customer).forEach(game -> {
            if (game.getKeys() > 0) {
                cartRepository.save(
                    buildCartEntry(
                        game,
                        customer
                    )
                );
            }
        });
    }

    @CustomerOnly
    public void removeFromWishlist(String gameId) {
        wishlistRepository.delete(
            buildWishlistEntry(
                    gameService.getGame(gameId),
                    getUser()
            )
        );
    }

    @CustomerOnly
    public void removeAllFromWishlist() {
        wishlistRepository.deleteById_CustomerId(getUser().getId());
    }
}
