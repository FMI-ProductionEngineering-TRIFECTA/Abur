package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.CustomerOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.LibraryEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.CartRepository;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.data.repository.LibraryRepository;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.CartInfo;
import ro.unibuc.hello.exception.GameAlreadyInListException;
import ro.unibuc.hello.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

import static ro.unibuc.hello.utils.ResponseUtils.*;
import static ro.unibuc.hello.data.entity.CartEntity.buildCartEntry;
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

    @Autowired
    private LibraryRepository libraryRepository;

    private ResponseEntity<?> getCartByCustomerId(String customerId) {
        UserEntity customer = userRepository.findByIdAndRole(customerId, Role.CUSTOMER);
        if (customer == null) throw new NotFoundException("No customer found at id %s", customerId);

        List<GameEntity> games = cartRepository.getGamesByCustomer(userRepository.findByIdAndRole(customerId, Role.CUSTOMER));
        double price = games.stream()
                            .mapToDouble(GameEntity::discountedPrice)
                            .sum();

        return ok(new CartInfo(price, games));
    }

    @CustomerOnly
    public ResponseEntity<?> getCart() {
        return getCartByCustomerId(getUser().getId());
    }

    @CustomerOnly
    public ResponseEntity<?> addGameToCartById(String gameId) {
        Optional<GameEntity> game = gameRepository.findById(gameId);
        if(game.isEmpty()) throw new NotFoundException("No game found at id %s", gameId);

        // check if the game is already in library
        if (libraryRepository.getGamesByCustomer(getUser())
                .stream()
                .anyMatch(g -> g.getId().equals(gameId))) {
            throw new GameAlreadyInListException(game.get().getTitle(),"library");
        }

        // check if the game is already in cart
        if (cartRepository.getGamesByCustomer(getUser())
                .stream()
                .anyMatch(g -> g.getId().equals(gameId))) {
            throw new GameAlreadyInListException(game.get().getTitle(),"cart");
        }

        return created(cartRepository.save(
                buildCartEntry(
                     game.get(),
                     getUser()
                )
        ));
    }

    @CustomerOnly
    public ResponseEntity<?> checkout() {
        List<GameEntity> valid_games = cartRepository
                .getGamesByCustomer(getUser())
                .stream()
                .filter(game -> game.getKeys() > 0)
                .toList();

        valid_games.forEach(game -> libraryRepository.save(
            LibraryEntity.buildLibraryEntry(
                game,
                getUser()
            )
        ));

        double price = valid_games.stream()
                .mapToDouble(GameEntity::discountedPrice)
                .sum();

        removeAllFromCart();
        return created(new CartInfo(price,valid_games));
    }

    @CustomerOnly
    public ResponseEntity<?> removeGameFromCart(String gameId) {
        Optional<GameEntity> game = gameRepository.findById(gameId);
        if(game.isEmpty()) throw new NotFoundException("No game found at id %s", gameId);

        cartRepository.delete(
                buildCartEntry(
                     game.get(),
                     getUser()
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
