package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.DeveloperOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.GameRepository;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.ErrorString;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.security.AuthenticationUtils;

import static ro.unibuc.hello.data.entity.GameEntity.buildDLC;
import static ro.unibuc.hello.data.entity.GameEntity.buildGame;
import static ro.unibuc.hello.utils.ResponseUtils.*;
import static ro.unibuc.hello.utils.ValidationUtils.*;

@Service
@SuppressWarnings("unchecked")
public class GameService {

    @Autowired
    protected GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    private static ResponseEntity<ErrorString> err;

    protected GameEntity.Type getType() {
        return GameEntity.Type.GAME;
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

    public ResponseEntity<?> createGame(Game gameInput) {
        UserEntity user = AuthenticationUtils.getAuthorizedUser(UserEntity.Role.DEVELOPER);
        if (user == null) return unauthorized();

        String title = gameInput.getTitle();
        if (gameRepository.findByTitle(title) != null) {
            return badRequest("The game %s already exists", title);
        }

        Double price = fallback(gameInput.getPrice(), 0.0);
        Integer discountPercentage = fallback(gameInput.getDiscountPercentage(), 0);
        Integer keys = fallback(gameInput.getKeys(), 100);
        err = chain
        (
                exists("Title", title),
                validate("Title", title, validLength(3)),
                validate("Price", price),
                validate("Discount percentage", discountPercentage),
                validate("Number of keys", keys)
        );
        if (err != null) return err;

        GameEntity savedGame = gameRepository.save(getType() == GameEntity.Type.GAME
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
                        gameInput.getBaseGame()
                )
        );

        user.getDetails().getGames().add(savedGame);
        userRepository.save(user);

        return created(savedGame);
    }

    @DeveloperOnly
    public ResponseEntity<?> updateGame(String id, Game gameInput) {
        GameEntity game = gameRepository.findByIdAndType(id, getType());
        err = chain
        (
                validateAndUpdate("Title", game::setTitle, gameInput.getTitle(), validLength(3)),
                validateAndUpdate("Price", game::setPrice, gameInput.getPrice()),
                validateAndUpdate("Discount percentage", game::setDiscountPercentage, gameInput.getDiscountPercentage())
        );

        return err != null ? err : ok(gameRepository.save(game));
    }

    @DeveloperOnly
    public ResponseEntity<?> addKeys(String id, Integer keys) {
        err = chain
        (
                validate("Number of keys", keys)
        );
        if (err != null) return err;

        GameEntity game = gameRepository.findByIdAndType(id, getType());
        game.setKeys(keys + game.getKeys());
        return ok(gameRepository.save(game));
    }

    @DeveloperOnly
    public ResponseEntity<?> markOutOfStock(String id) {
        GameEntity game = gameRepository.findByIdAndType(id, getType());
        game.setKeys(0);
        return ok(gameRepository.save(game));
    }

    @DeveloperOnly
    public ResponseEntity<?> deleteGame(String id) {
        gameRepository.delete(gameRepository.findByIdAndType(id, getType()));
        return noContent();
    }
}
