package ro.unibuc.hello.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.CartEntity;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.CartRepository;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.data.repository.LibraryRepository;
import ro.unibuc.hello.data.repository.WishlistRepository;
import ro.unibuc.hello.dto.CartInfo;
import ro.unibuc.hello.exception.ValidationException;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static ro.unibuc.hello.data.entity.CartEntity.buildCartEntry;
import static ro.unibuc.hello.data.entity.GameEntity.*;
import static ro.unibuc.hello.data.entity.LibraryEntity.buildLibraryEntry;
import static ro.unibuc.hello.security.AuthenticationUtils.getUser;
import static ro.unibuc.hello.utils.DatabaseUtils.CompositeKey.build;
import static ro.unibuc.hello.utils.ValidationUtils.validate;

@Service
public class CartService {

    @Autowired
    protected CartRepository cartRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private MeterRegistry metricsRegistry;

    private final AtomicLong counter = new AtomicLong();

    private CartInfo getCartByCustomerId(String customerId) {
        List<GameEntity> games = cartRepository.getGamesByCustomer(customerService.getCustomer(customerId));
        return new CartInfo(totalPrice(games), games);
    }

    @CustomerOnly
    public CartInfo getCart() {
        metricsRegistry
                .counter("my_non_aop_metric", "endpoint", "cart")
                .increment(counter.incrementAndGet());

        return getCartByCustomerId(getUser().getId());
    }

    @CustomerOnly
    public synchronized void checkout() {
        metricsRegistry
                .counter("my_non_aop_metric", "endpoint", "checkout")
                .increment(counter.incrementAndGet());

        UserEntity customer = getUser();
        List<GameEntity> games = cartRepository.getGamesByCustomer(customer);

        games.forEach(GameEntity::decreaseNoKeys);
        games.forEach(game -> {
            gameRepository.save(game);
            customer.getGames().add(game);
        });
        games.forEach(game -> libraryRepository.save(
                buildLibraryEntry(
                        game,
                        customer
                )
        ));
        games.forEach(game -> wishlistRepository.deleteById(build(game, getUser())));

        removeAllFromCart();
    }

    @CustomerOnly
    public CartEntity addToCart(String gameId) {
        UserEntity customer = getUser();
        GameEntity game = gameService.getGame(gameId);

        validate(
                game.getTitle(),
                gameId,
                notInCart(cartRepository.getGamesByCustomer(customer))
                .and(notInLibrary(libraryRepository.getGamesByCustomer(customer)))
        );
        if (game.getKeys() == 0) throw new ValidationException("%s is not in stock!", game.getTitle());

        return cartRepository.save(
            buildCartEntry(
                 game,
                 customer
            )
        );
    }

    @CustomerOnly
    public void removeFromCart(String gameId) {
        cartRepository.delete(
            buildCartEntry(
                gameService.getGame(gameId),
                getUser()
            )
        );
    }

    @CustomerOnly
    public void removeAllFromCart() {
        cartRepository.deleteById_CustomerId(getUser().getId());
    }
}
