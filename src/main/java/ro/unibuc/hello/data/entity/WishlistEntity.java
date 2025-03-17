package ro.unibuc.hello.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import static ro.unibuc.hello.utils.DatabaseUtils.*;
import static ro.unibuc.hello.utils.DatabaseUtils.CompositeKey.build;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("wishlist")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WishlistEntity {

    @Id
    private CompositeKey id;

    @DBRef
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GameEntity game;

    @DBRef
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserEntity customer;

    public static WishlistEntity buildWishlistEntry(GameEntity game, UserEntity customer) {
        return WishlistEntity
                .builder()
                .id(build(game, customer))
                .game(game)
                .customer(customer)
                .build();
    }

}
