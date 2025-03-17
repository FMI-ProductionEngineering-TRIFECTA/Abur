package ro.unibuc.hello.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

import static ro.unibuc.hello.data.entity.GameEntity.*;

@Repository
public interface GameRepository extends MongoRepository<GameEntity, String> {

    GameEntity findByIdAndType(String id, Type type);
    GameEntity findByTitle(String title);
    List<GameEntity> findByType(Type type);

    default GameEntity getGame(String gameId) {
        Optional<GameEntity> game = findById(gameId);
        if (game.isEmpty()) throw new NotFoundException("No game found at id %s", gameId);
        return game.get();
    }

}
