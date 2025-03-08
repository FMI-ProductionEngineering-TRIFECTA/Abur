package ro.unibuc.hello.data.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

import static ro.unibuc.hello.utils.DateUtils.parseDate;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("games")
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

    private Date releaseDate;

    @DBRef
    private UserEntity developer;

    private Type type;

    @DBRef
    private GameEntity baseGame;

    public GameEntity(String title, Double price, Integer discountPercentage, Date releaseDate, UserEntity developer, Type type, GameEntity baseGame) {
        this.title = title;
        this.price = price;
        this.discountPercentage = discountPercentage;
        this.releaseDate = releaseDate;
        this.developer = developer;
        this.type = type;
        this.baseGame = baseGame;
    }

    public static GameEntity buildGame(String id, String title, Double price, String releaseDate, UserEntity developer) {
        return GameEntity
                .builder()
                .id(id)
                .title(title)
                .price(price)
                .discountPercentage(0)
                .releaseDate(parseDate(releaseDate))
                .developer(developer)
                .type(Type.GAME)
                .build();
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
        return dlc;
    }
}
