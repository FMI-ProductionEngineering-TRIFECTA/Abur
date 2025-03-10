package ro.unibuc.hello.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

import static ro.unibuc.hello.utils.DateUtils.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("library")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LibraryEntity {
    @Id
    private CompositeKey id;

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    static class CompositeKey implements Serializable {
        private String gameId;
        private String customerId;

        public static CompositeKey build(GameEntity game, UserEntity customer) {
            return new CompositeKey(game.getId(), customer.getId());
        }
    }

    private Date purchaseDate;

    @DBRef
    @JsonIgnore
    private GameEntity game;

    @DBRef
    @JsonIgnore
    private UserEntity customer;

    public static LibraryEntity build(GameEntity game, UserEntity customer) {
        return LibraryEntity
                .builder()
                .id(CompositeKey.build(game, customer))
                .purchaseDate(dateNow())
                .game(game)
                .customer(customer)
                .build();
    }
}
