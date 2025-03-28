package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.DeveloperOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.data.repository.*;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.exception.NotFoundException;
import ro.unibuc.hello.exception.UnauthorizedAccessException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ro.unibuc.hello.data.entity.GameEntity.*;
import static ro.unibuc.hello.security.AuthenticationUtils.getUser;
import static ro.unibuc.hello.utils.ValidationUtils.*;

@Service
public class GameService {

    @Autowired
    protected GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    private void deleteGameDependencies(GameEntity game) {
        String gameId = game.getId();
        libraryRepository.deleteById_GameId(gameId);
        cartRepository.deleteById_GameId(gameId);
        wishlistRepository.deleteById_GameId(gameId);
        userRepository.findByIdAndRole(game.getDeveloper().getId(), UserEntity.Role.DEVELOPER).getGames().remove(game);
    }

    protected Type getType() {
        return Type.GAME;
    }

    public GameEntity getGame(String gameId) {
        Optional<GameEntity> game = gameRepository.findById(gameId);
        if (game.isEmpty()) throw new NotFoundException("No game found at id %s!", gameId);
        return game.get();
    }

    private GameEntity getAndValidateOwnership(String id, UserEntity user) {
        GameEntity game = getGame(id);
        if (!Objects.equals(user.getUsername(), game.getDeveloper().getUsername())) throw new UnauthorizedAccessException();
        return game;
    }

    private GameEntity getAndAssureType(String gameId) {
        GameEntity game = getGame(gameId);
        if (game.getType() != getType()) throw new NotFoundException("%s is not a %s", game.getTitle(), getType().toString().toLowerCase());
        return game;
    }

    public GameEntity getGameById(String id) {
        return getAndAssureType(id);
    }

    public List<GameEntity> getAllGames() {
        return gameRepository.findByType(getType());
    }

    public List<GameEntity> getGameDLCs(String id) {
        return getAndAssureType(id).getDlcs();
    }

    @DeveloperOnly
    public GameEntity createGame(Game gameInput) {
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

        return savedGame;
    }

    @DeveloperOnly
    public GameEntity updateGame(String id, Game gameInput) {
        GameEntity game = getAndValidateOwnership(id, getUser());

        String title = gameInput.getTitle();
        validate(String.format("Title %s", title), title, isUnique(() -> gameRepository.findByTitle(title)));
        validateAndUpdate("Title", game::setTitle, gameInput.getTitle(), validLength(3));

        validateAndUpdate("Price", game::setPrice, gameInput.getPrice());
        validateAndUpdate("Discount percentage", game::setDiscountPercentage, gameInput.getDiscountPercentage());

        return gameRepository.save(game);
    }

    @DeveloperOnly
    public GameEntity addKeys(String id, Integer keys) {
        GameEntity game = getAndValidateOwnership(id, getUser());

        validate("Number of keys", keys);

        game.setKeys(keys + game.getKeys());
        return gameRepository.save(game);
    }

    @DeveloperOnly
    public GameEntity markOutOfStock(String id) {
        GameEntity game = getAndValidateOwnership(id, getUser());
        game.setKeys(0);
        return gameRepository.save(game);
    }

    @DeveloperOnly
    public void deleteGame(String id) {
        GameEntity game = getAndValidateOwnership(id, getUser());
        List<GameEntity> dlcs = game.getDlcs();

        dlcs.forEach(this::deleteGameDependencies);
        gameRepository.deleteAll(dlcs);

        deleteGameDependencies(game);
        gameRepository.delete(game);
    }

}
