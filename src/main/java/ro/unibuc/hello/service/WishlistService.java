package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.data.repository.LibraryRepository;
import ro.unibuc.hello.data.repository.WishlistRepository;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.exception.ValidationException;

import java.util.List;
import java.util.Optional;

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

    private GameEntity getGame(String gameId) {
        Optional<GameEntity> game = gameRepository.findById(gameId);
        if (game.isEmpty()) throw new NotFoundException("No game found at id %s", gameId);
        return game.get();
    }

    private void validateGame(List<GameEntity> list, GameEntity game, String listName) {
        if (list.stream().anyMatch(g -> g.getId().equals(game.getId()))) {
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
    public ResponseEntity<?> addGameToWishlist(String gameId) {
        UserEntity customer = getUser();
        GameEntity game = getGame(gameId);

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
