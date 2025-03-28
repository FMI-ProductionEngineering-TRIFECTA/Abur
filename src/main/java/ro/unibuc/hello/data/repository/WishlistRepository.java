package ro.unibuc.hello.data.repository;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.WishlistEntity;

import java.util.function.Function;

import static ro.unibuc.hello.utils.DatabaseUtils.CompositeKey;

@Repository
public interface WishlistRepository extends GameCollectionRepository<WishlistEntity, CompositeKey> {

    void deleteById(@NonNull CompositeKey id);

    @Override
    default Function<WishlistEntity, GameEntity> getGameMapper() {
        return WishlistEntity::getGame;
    }

}
