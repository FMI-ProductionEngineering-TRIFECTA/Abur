package ro.unibuc.hello.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.entity.LibraryEntity;
import ro.unibuc.hello.data.entity.LibraryEntity.CompositeKey;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;

import java.util.List;

@Repository
public interface LibraryRepository extends MongoRepository<LibraryEntity, CompositeKey> {
    List<LibraryEntity> findById_CustomerId(String customerId);

    default List<GameEntity> getGamesByCustomer(UserEntity customer) {
        return findById_CustomerId(customer.getId()).stream()
                .map(LibraryEntity::getGame)
                .toList();
    }

}
