package ro.unibuc.hello.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;

import java.util.List;
import java.util.function.Function;

@NoRepositoryBean
public interface GameCollectionRepository<T, K> extends MongoRepository<T, K> {

    void deleteById_CustomerId(String customerId);
    void deleteById_GameId(String gameId);
    List<T> findById_CustomerId(String customerId);

    default List<GameEntity> getGamesByCustomer(UserEntity customer) {
        return findById_CustomerId(customer.getId())
                .stream()
                .map(getGameMapper())
                .toList();
    }

    Function<T, GameEntity> getGameMapper();

}
