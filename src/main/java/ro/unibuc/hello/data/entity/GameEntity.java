package ro.unibuc.hello.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ro.unibuc.hello.utils.DateUtils.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("games")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameEntity {

    public enum Type {
        GAME,
        DLC
    }

    @Id
    private String id;

    @Indexed(unique = true)
    private String title;

    private Double price;

    private Integer discountPercentage;

    private Integer keys;

    private Date releaseDate;

    @DBRef
    @JsonIgnore
    private UserEntity developer;

    private Type type;

    @DBRef
    @JsonIgnore
    private GameEntity baseGame;

    @DBRef
    @JsonIgnore
    private List<GameEntity> dlcs;

    public static GameEntity buildGame(String title, Double price, Integer discountPercentage, Integer keys, UserEntity developer) {
        return GameEntity
                .builder()
                .title(title)
                .price(price)
                .discountPercentage(discountPercentage)
                .keys(keys)
                .releaseDate(dateNow())
                .developer(developer)
                .type(Type.GAME)
                .dlcs(new ArrayList<>())
                .build();
    }

    public static GameEntity buildGame(String id, String title, Double price, String releaseDate, UserEntity developer) {
        return GameEntity
                .builder()
                .id(id)
                .title(title)
                .price(price)
                .discountPercentage(0)
                .keys(100)
                .releaseDate(parseDate(releaseDate))
                .developer(developer)
                .type(Type.GAME)
                .dlcs(new ArrayList<>())
                .build();
    }

    public static GameEntity buildDLC(String title, Double price, Integer discountPercentage, Integer keys, UserEntity developer, GameEntity baseGame) {
        GameEntity dlc = buildGame(
                title,
                price,
                discountPercentage,
                keys,
                developer
        );

        dlc.setType(Type.DLC);
        dlc.setBaseGame(baseGame);
        dlc.setDlcs(null);
        return dlc;
    }

    public static GameEntity buildDLC(String id, String title, Double price, String releaseDate, UserEntity developer, GameEntity baseGame) {
        GameEntity dlc = buildGame(
                id,
                title,
                price,
                releaseDate,
                developer
        );

        dlc.setType(Type.DLC);
        dlc.setBaseGame(baseGame);
        dlc.setDlcs(null);
        return dlc;
    }
}
