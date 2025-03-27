package ro.unibuc.hello.data.repository;

import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.entity.CartEntity;
import ro.unibuc.hello.data.entity.GameEntity;

import java.util.function.Function;

import static ro.unibuc.hello.utils.DatabaseUtils.CompositeKey;

@Repository
public interface CartRepository extends GameCollectionRepository<CartEntity, CompositeKey> {

    @Override
    default Function<CartEntity, GameEntity> getGameMapper() {
        return CartEntity::getGame;
    }

}
