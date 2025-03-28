package ro.unibuc.hello.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.entity.GameEntity;

import java.util.List;

import static ro.unibuc.hello.data.entity.GameEntity.Type;

@Repository
public interface GameRepository extends MongoRepository<GameEntity, String> {

    GameEntity findByIdAndType(String id, Type type);
    GameEntity findByTitle(String title);
    List<GameEntity> findByType(Type type);

}
