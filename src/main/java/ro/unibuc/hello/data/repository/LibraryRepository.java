package ro.unibuc.hello.data.repository;

import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.LibraryEntity;

import java.util.function.Function;

import static ro.unibuc.hello.utils.DatabaseUtils.CompositeKey;

@Repository
public interface LibraryRepository extends GameCollectionRepository<LibraryEntity, CompositeKey> {

    @Override
    default Function<LibraryEntity, GameEntity> getGameMapper() {
        return LibraryEntity::getGame;
    }

}
