package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.DeveloperOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.exception.UnauthorizedAccessException;

import java.util.Objects;

import static ro.unibuc.hello.data.entity.GameEntity.*;
import static ro.unibuc.hello.security.AuthenticationUtils.*;
import static ro.unibuc.hello.utils.ResponseUtils.*;
import static ro.unibuc.hello.utils.ValidationUtils.*;

@Service
public class GameService {

    @Autowired
    protected GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    private GameEntity validateGameOwnership(String id, UserEntity user) {
        GameEntity game = gameRepository.findByIdAndType(id, getType());

        if (game == null) throw new NotFoundException("No game found at id %s", id);
        if (user == null || !Objects.equals(user.getUsername(), game.getDeveloper().getUsername())) throw new UnauthorizedAccessException();

        return game;
    }

    protected Type getType() {
        return Type.GAME;
    }

    public ResponseEntity<?> getGameById(String id) {
        return ok(gameRepository.findByIdAndType(id, getType()));
    }

    public ResponseEntity<?> getAllGames() {
        return ok(gameRepository.findByType(getType()));
    }

    public ResponseEntity<?> getGameDLCs(String id) {
        return ok(gameRepository.findByIdAndType(id, getType()).getDlcs());
    }

    @DeveloperOnly
    public ResponseEntity<?> createGame(Game gameInput) {
        UserEntity user = getUser();

        GameEntity baseGame = gameInput.getBaseGame();
        if (getType() == Type.DLC && !baseGame.getDeveloper().getUsername().equals(user.getUsername())) throw new UnauthorizedAccessException();

        String title = gameInput.getTitle();
        Double price = fallback(gameInput.getPrice(), 0.0);
        Integer discountPercentage = fallback(gameInput.getDiscountPercentage(), 0);
        Integer keys = fallback(gameInput.getKeys(), 100);

        exists("Title", title);
        validate("Title", title, validLength(3));
        validate(String.format("Title %s", title), title, isUnique(() -> gameRepository.findByTitle(title)));
        validate("Price", price);
        validate("Discount percentage", discountPercentage);
        validate("Number of keys", keys);

        GameEntity savedGame = gameRepository.save(getType() == Type.GAME
                ? buildGame(
                        title,
                        price,
                        discountPercentage,
                        keys,
                        user
                )
                : buildDLC(
                        title,
                        price,
                        discountPercentage,
                        keys,
                        user,
                        baseGame
                )
        );

        user.getGames().add(savedGame);
        userRepository.save(user);

        return created(savedGame);
    }

    @DeveloperOnly
    public ResponseEntity<?> updateGame(String id, Game gameInput) {
        GameEntity game = validateGameOwnership(id, getUser());

        String title = gameInput.getTitle();
        validate(String.format("Title %s", title), title, isUnique(() -> gameRepository.findByTitle(title)));
        validateAndUpdate("Title", game::setTitle, gameInput.getTitle(), validLength(3));

        validateAndUpdate("Price", game::setPrice, gameInput.getPrice());
        validateAndUpdate("Discount percentage", game::setDiscountPercentage, gameInput.getDiscountPercentage());

        return ok(gameRepository.save(game));
    }

    @DeveloperOnly
    public ResponseEntity<?> addKeys(String id, Integer keys) {
        GameEntity game = validateGameOwnership(id, getUser());

        validate("Number of keys", keys);

        game.setKeys(keys + game.getKeys());
        return ok(gameRepository.save(game));
    }

    @DeveloperOnly
    public ResponseEntity<?> markOutOfStock(String id) {
        GameEntity game = validateGameOwnership(id, getUser());
        game.setKeys(0);
        return ok(gameRepository.save(game));
    }

    @DeveloperOnly
    public ResponseEntity<?> deleteGame(String id) {
        GameEntity game = validateGameOwnership(id, getUser());
        gameRepository.delete(game);
        return noContent();
    }

}
