package ro.unibuc.hello.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.entity.CartEntity;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;

import java.util.List;

import static ro.unibuc.hello.utils.DatabaseUtils.*;

@Repository
public interface CartRepository extends MongoRepository<CartEntity, CompositeKey> {

    List<CartEntity> findById_CustomerId(String customerId);
    void deleteById_CustomerId(String customerId);

    default List<GameEntity> getGamesByCustomer(UserEntity customer) {
        return findById_CustomerId(customer.getId()).stream()
                .map(CartEntity::getGame)
                .toList();
    }
}