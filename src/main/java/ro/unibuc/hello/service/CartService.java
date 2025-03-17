package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.CartRepository;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.data.repository.LibraryRepository;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.CartInfo;
import ro.unibuc.hello.dto.User;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.exception.ValidationException;

import java.util.List;
import java.util.Optional;

import static ro.unibuc.hello.utils.ResponseUtils.*;
import static ro.unibuc.hello.data.entity.GameEntity.totalPrice;
import static ro.unibuc.hello.data.entity.CartEntity.buildCartEntry;
import static ro.unibuc.hello.data.entity.LibraryEntity.buildLibraryEntry;
import static ro.unibuc.hello.data.entity.UserEntity.Role;
import static ro.unibuc.hello.security.AuthenticationUtils.*;

@Service
public class CartService {

    @Autowired
    protected CartRepository cartRepository;

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
    public ResponseEntity<?> addGameToCartById(String gameId) {
        UserEntity customer = getUser();
        GameEntity game = getGame(gameId);

        validateGame(libraryRepository.getGamesByCustomer(customer), game, "library");
        validateGame(cartRepository.getGamesByCustomer(customer), game, "cart");
        if (game.getKeys() == 0) throw new ValidationException("The game %s is not in stock.", game.getTitle());

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

        removeAllFromCart();
        return created(new CartInfo(totalPrice(games), games));
    }

    @CustomerOnly
    public ResponseEntity<?> removeGameFromCart(String gameId) {
        GameEntity game = getGame(gameId);
        UserEntity customer = getUser();

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
