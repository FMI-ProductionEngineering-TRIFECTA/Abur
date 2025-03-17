package ro.unibuc.hello.service;

import org.apache.tomcat.jni.Library;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.*;
import ro.unibuc.hello.dto.CartInfo;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.exception.ValidationException;

import java.util.List;

import static ro.unibuc.hello.service.GameService.getGame;
import static ro.unibuc.hello.utils.DatabaseUtils.CompositeKey.build;
import static ro.unibuc.hello.utils.ResponseUtils.*;
import static ro.unibuc.hello.data.entity.GameEntity.totalPrice;
import static ro.unibuc.hello.data.entity.CartEntity.buildCartEntry;
import static ro.unibuc.hello.data.entity.LibraryEntity.buildLibraryEntry;
import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.security.AuthenticationUtils.*;
import static ro.unibuc.hello.utils.ValidationUtils.*;

@Service
public class CartService {

    @Autowired
    protected CartRepository cartRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    private ResponseEntity<?> getCartByCustomerId(String customerId) {
        UserEntity customer = userRepository.findByIdAndRole(customerId, Role.CUSTOMER);
        if (customer == null) throw new NotFoundException("No customer found at id %s", customerId);

        List<GameEntity> games = cartRepository.getGamesByCustomer(customer);
        return ok(new CartInfo(totalPrice(games), games));
    }

    @CustomerOnly
    public ResponseEntity<?> getCart() {
        return getCartByCustomerId(getUser().getId());
    }

    @CustomerOnly
    public ResponseEntity<?> addToCart(String gameId) {
        UserEntity customer = getUser();
        GameEntity game = getGame(gameRepository, gameId);
        String title = game.getTitle();

        validate(title, gameId, isNotIn(() -> cartRepository.getGamesByCustomer(customer), "cart"));
        validate(title, gameId, isNotIn(() -> libraryRepository.getGamesByCustomer(customer), "library"));
        if (game.getKeys() == 0) throw new ValidationException("The game %s is not in stock!", game.getTitle());

        return created(cartRepository.save(
            buildCartEntry(
                 game,
                 customer
            )
        ));
    }

    @CustomerOnly
    public synchronized ResponseEntity<?> checkout() {
        UserEntity customer = getUser();
        List<GameEntity> games = cartRepository.getGamesByCustomer(customer);

        games.forEach(GameEntity::decreaseNoKeys);
        games.forEach(game -> libraryRepository.save(
            buildLibraryEntry(
                game,
                customer
            )
        ));
        games.forEach(game -> wishlistRepository.deleteById(build(game, getUser())));

        removeAllFromCart();
        return noContent();
    }

    @CustomerOnly
    public ResponseEntity<?> removeFromCart(String gameId) {
        UserEntity customer = getUser();
        GameEntity game = getGame(gameRepository, gameId);

        cartRepository.delete(
            buildCartEntry(
                 game,
                 customer
            )
        );
        return noContent();
    }

    @CustomerOnly
    public ResponseEntity<?> removeAllFromCart() {
        cartRepository.deleteById_CustomerId(getUser().getId());
        return noContent();
    }
}
