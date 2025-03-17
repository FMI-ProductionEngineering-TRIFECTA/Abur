package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.*;
import ro.unibuc.hello.exception.ValidationException;

import java.util.List;

import static ro.unibuc.hello.data.entity.CartEntity.buildCartEntry;
import static ro.unibuc.hello.data.entity.WishlistEntity.buildWishlistEntry;
import static ro.unibuc.hello.service.CartService.addToCartConditions;
import static ro.unibuc.hello.service.GameService.getGame;
import static ro.unibuc.hello.service.GameService.validateGame;
import static ro.unibuc.hello.utils.ResponseUtils.*;
import static ro.unibuc.hello.security.AuthenticationUtils.*;

@Service
public class WishlistService {

    @Autowired
    protected WishlistRepository wishlistRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CartRepository cartRepository;

    private void validateGameInWishlist(List<GameEntity> list, GameEntity game) {
        if (list.stream().noneMatch(g -> g.getId().equals(game.getId()))) {
            throw new ValidationException("%s is not in wishlist", game.getTitle());
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
        GameEntity game = getGame(gameRepository, gameId);

        validateGame(wishlistRepository.getGamesByCustomer(customer), game, "wishlist");
        validateGame(libraryRepository.getGamesByCustomer(customer), game, "library");

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

        validateGameInWishlist(wishlistRepository.getGamesByCustomer(customer), game);
        addToCartConditions(game, customer, libraryRepository, cartRepository, Boolean.TRUE);

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

        games.forEach(game -> {
            // If it has 0 keys, it just remains in Wishlist
            addToCartConditions(game, customer, libraryRepository, cartRepository, Boolean.FALSE);
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
        UserEntity customer = getUser();
        GameEntity game = getGame(gameRepository, gameId);

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
