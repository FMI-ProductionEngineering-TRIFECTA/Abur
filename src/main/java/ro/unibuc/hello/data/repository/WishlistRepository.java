package ro.unibuc.hello.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.entity.WishlistEntity;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;

import java.util.List;

import static ro.unibuc.hello.utils.DatabaseUtils.*;

@Repository
public interface WishlistRepository extends MongoRepository<WishlistEntity, CompositeKey> {
    List<WishlistEntity> findById_CustomerId(String customerId);
    void deleteById_CustomerId(String customerId);
    void deleteById(CompositeKey id);

    default List<GameEntity> getGamesByCustomer(UserEntity customer) {
        return findById_CustomerId(customer.getId()).stream()
                .map(WishlistEntity::getGame)
                .toList();
    }

}
