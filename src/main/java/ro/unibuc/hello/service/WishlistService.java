package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.*;

import java.util.List;

import static ro.unibuc.hello.data.entity.CartEntity.buildCartEntry;
import static ro.unibuc.hello.data.entity.WishlistEntity.buildWishlistEntry;
import static ro.unibuc.hello.service.GameService.getGame;
import static ro.unibuc.hello.utils.ResponseUtils.*;
import static ro.unibuc.hello.security.AuthenticationUtils.*;
import static ro.unibuc.hello.utils.ValidationUtils.*;

@Service
public class WishlistService {

    @Autowired
    protected WishlistRepository wishlistRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @CustomerOnly
    public ResponseEntity<?> getWishlist() {
        return ok(wishlistRepository.getGamesByCustomer(getUser()));
    }

    @CustomerOnly
    public ResponseEntity<?> addToWishlist(String gameId) {
        UserEntity customer = getUser();
        GameEntity game = getGame(gameRepository, gameId);
        String title = game.getTitle();

        validate(title, gameId, isNotIn(() -> wishlistRepository.getGamesByCustomer(customer), "wishlist"));
        validate(title, gameId, isNotIn(() -> libraryRepository.getGamesByCustomer(customer), "library"));

        return created(wishlistRepository.save(
            buildWishlistEntry(
                    game,
                    customer
            )
        ));
    }

    @CustomerOnly
    public ResponseEntity<?> moveToCart(String gameId) {
        UserEntity customer = getUser();
        GameEntity game = getGame(gameRepository, gameId);
        String title = game.getTitle();

        validate(title, gameId, isIn(() -> wishlistRepository.getGamesByCustomer(customer), "wishlist"));
        validate(title, gameId, isNotIn(() -> cartRepository.getGamesByCustomer(customer), "cart"));

        cartRepository.save(
            buildCartEntry(
                game,
                customer
            )
        );

        return noContent();
    }

    @CustomerOnly
    public ResponseEntity<?> moveAllToCart() {
        UserEntity customer = getUser();
        List<GameEntity> games = wishlistRepository.getGamesByCustomer(customer);

        games.forEach(game -> {
            if (game.getKeys() > 0) {
                cartRepository.save(
                    buildCartEntry(
                        game,
                        customer
                    )
                );
            }
        });

        return noContent();
    }

    @CustomerOnly
    public ResponseEntity<?> removeFromWishlist(String gameId) {
        wishlistRepository.delete(
            buildWishlistEntry(
                    getGame(gameRepository, gameId),
                    getUser()
            )
        );

        return noContent();
    }

    @CustomerOnly
    public ResponseEntity<?> removeAllFromWishlist() {
        wishlistRepository.deleteById_CustomerId(getUser().getId());
        return noContent();
    }
}
