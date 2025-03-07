package ro.unibuc.hello.dto;

import lombok.Getter;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;

import java.util.Date;

@Getter
public class Game {

    private String title;
    private Double price;
    private Integer discountPercentage;
    private Date releaseDate;
    private UserEntity developer;
    private GameEntity.Type type;

}
