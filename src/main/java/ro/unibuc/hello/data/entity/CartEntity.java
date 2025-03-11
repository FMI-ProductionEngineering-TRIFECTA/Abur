package ro.unibuc.hello.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import static ro.unibuc.hello.utils.DateUtils.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("cart")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartEntity {
    @Id
    private LibraryEntity.CompositeKey id;

    @DBRef
    @JsonIgnore
    private GameEntity game;

    @DBRef
    @JsonIgnore
    private UserEntity customer;

    public static CartEntity build(GameEntity game, UserEntity customer) {
        return CartEntity
                .builder()
                .id(LibraryEntity.CompositeKey.build(game, customer))
                .game(game)
                .customer(customer)
                .build();
    }
}
