package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.*;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.exception.ValidationException;

import java.util.List;
import java.util.Optional;

import static ro.unibuc.hello.data.entity.CartEntity.buildCartEntry;
import static ro.unibuc.hello.data.entity.WishlistEntity.buildWishlistEntry;
import static ro.unibuc.hello.utils.ResponseUtils.*;
import static ro.unibuc.hello.security.AuthenticationUtils.*;

@Service
public class WishlistService {

    @Autowired
    protected WishlistRepository wishlistRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CartRepository cartRepository;

    private GameEntity getGame(String gameId) {
        Optional<GameEntity> game = gameRepository.findById(gameId);
        if (game.isEmpty()) throw new NotFoundException("No game found at id %s", gameId);
        return game.get();
    }

    private void validateGame(List<GameEntity> list, GameEntity game, String listName, Boolean reverseCondition) {
        if (reverseCondition && list.stream().anyMatch(g -> g.getId().equals(game.getId()))) {
            throw new ValidationException("%s already in %s", game.getTitle(), listName);
        }
    }

    @Autowired
    private LibraryRepository libraryRepository;

    @CustomerOnly
    public ResponseEntity<?> getWishlist() {
        return ok(wishlistRepository.getGamesByCustomer(getUser()));
    }

    @CustomerOnly
    public ResponseEntity<?> addToWishlist(String gameId) {
        UserEntity customer = getUser();
        GameEntity game = getGame(gameId);

        validateGame(wishlistRepository.getGamesByCustomer(customer), game, "wishlist", Boolean.TRUE);
        validateGame(libraryRepository.getGamesByCustomer(customer), game, "library", Boolean.TRUE);

        return created(wishlistRepository.save(
            buildWishlistEntry(
                    game,
                    customer
            )
        ));
    }

    @CustomerOnly
    public ResponseEntity<?> moveToCart(String gameId) {
        GameEntity game = getGame(gameId);
        UserEntity customer = getUser();

        validateGame(wishlistRepository.getGamesByCustomer(customer), game, "wishlist", Boolean.FALSE);

        // add to cart
        validateGame(libraryRepository.getGamesByCustomer(customer), game, "library", Boolean.TRUE);
        validateGame(cartRepository.getGamesByCustomer(customer), game, "cart", Boolean.TRUE);
        if (game.getKeys() == 0) throw new ValidationException("The game %s is not in stock!", game.getTitle());

        cartRepository.save(
            buildCartEntry(
                game,
                customer
            )
        );
        wishlistRepository.delete(
            buildWishlistEntry(
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

        // add to cart
        games.forEach(game -> {
            validateGame(libraryRepository.getGamesByCustomer(customer), game, "library", Boolean.TRUE);
            validateGame(cartRepository.getGamesByCustomer(customer), game, "cart", Boolean.TRUE);
            // If it has 0 keys, it just remains in Wishlist
            if (game.getKeys() > 0) {
                cartRepository.save(
                    buildCartEntry(
                        game,
                        customer
                    )
                );
                wishlistRepository.delete(
                        buildWishlistEntry(
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
        GameEntity game = getGame(gameId);
        UserEntity customer = getUser();

        wishlistRepository.delete(
            buildWishlistEntry(
                    game,
                    customer
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
