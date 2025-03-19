package ro.unibuc.hello.utils;

import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;

public interface GameTestUtils {
    static List<GameEntity> buildGames(Integer total) {
        List<GameEntity> games = new ArrayList<>();
        for (int id = 1; id <= total; ++id) {
            games.add(GameEntity
                    .builder()
                    .id(String.valueOf(id))
                    .title(String.format("Game %d", id))
                    .price(0.0)
                    .discountPercentage(0)
                    .keys(100)
                    .type(GameEntity.Type.GAME)
                    .dlcs(new ArrayList<>())
                    .build()
            );
        }
        return games;
    }

    static List<GameEntity> buildDLCs(Integer total, GameEntity baseGame) {
        List<GameEntity> dlcs = new ArrayList<>();
        for (int id = 1; id <= total; ++id) {
            dlcs.add(GameEntity
                    .builder()
                    .title(String.format("%s DLC %d", baseGame.getTitle(), id))
                    .type(GameEntity.Type.DLC)
                    .build()
            );
        }
        baseGame.getDlcs().addAll(dlcs);
        return dlcs;
    }

    static GameEntity buildGame() {
        return buildGames(1).getFirst();
    }

    static GameEntity buildGame(UserEntity developer) {
        GameEntity game = buildGame();
        game.setDeveloper(developer);
        return game;
    }
}
