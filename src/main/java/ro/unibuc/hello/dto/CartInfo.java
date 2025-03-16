package ro.unibuc.hello.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ro.unibuc.hello.data.entity.GameEntity;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CartInfo {

    private Double price;
    private List<GameEntity> items;

}
