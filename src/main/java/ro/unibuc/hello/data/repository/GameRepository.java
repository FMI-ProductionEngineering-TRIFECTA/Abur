package ro.unibuc.hello.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.entity.GameEntity;

import java.util.List;

/**
 * No need to implement this interface.
 * Spring Data MongoDB automatically creates a class it implementing the interface when you run the application.
 */
@Repository
public interface GameRepository extends MongoRepository<GameEntity, String> {

    GameEntity findByIdAndType(String id, GameEntity.Type type);
    List<GameEntity> findByType(GameEntity.Type type);

}
