package ro.unibuc.hello.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

import static ro.unibuc.hello.utils.DateUtils.*;
import static ro.unibuc.hello.utils.DatabaseUtils.*;
import static ro.unibuc.hello.utils.DatabaseUtils.CompositeKey.build;

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

    @EqualsAndHashCode.Exclude
    private Date purchaseDate;

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

    public static LibraryEntity buildLibraryEntry(GameEntity game, UserEntity customer) {
        return LibraryEntity
                .builder()
                .id(build(game, customer))
                .purchaseDate(dateNow())
                .game(game)
                .customer(customer)
                .build();
    }

}
