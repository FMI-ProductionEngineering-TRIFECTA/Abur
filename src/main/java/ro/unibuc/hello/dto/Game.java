package ro.unibuc.hello.dto;

import lombok.*;
import ro.unibuc.hello.data.entity.GameEntity;

@Getter
@Setter
@Builder
public class Game {

    private String title;
    private Double price;
    private Integer discountPercentage;
    private Integer keys;
    private GameEntity baseGame;

}
