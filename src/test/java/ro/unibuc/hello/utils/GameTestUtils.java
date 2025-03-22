package ro.unibuc.hello.utils;

import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;

public final class GameTestUtils {

    private GameTestUtils() {}

    public static List<GameEntity> buildGames(int total) {
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

    public static GameEntity buildGame() {
        return buildGames(1).getFirst();
    }

    public static GameEntity buildGame(UserEntity developer) {
        GameEntity game = buildGame();
        game.setDeveloper(developer);
        return game;
    }

    public static List<GameEntity> buildDLCsForGame(int total, GameEntity baseGame) {
        List<GameEntity> dlcs = new ArrayList<>();
        for (int id = 1; id <= total; ++id) {
            dlcs.add(GameEntity
                    .builder()
                    .id(String.format("%s-DLC", baseGame.getId()))
                    .title(String.format("%s DLC %d", baseGame.getTitle(), id))
                    .price(0.0)
                    .discountPercentage(0)
                    .keys(100)
                    .type(GameEntity.Type.DLC)
                    .developer(baseGame.getDeveloper())
                    .build()
            );
        }
        return dlcs;
    }

    public static GameEntity buildDLCForGame(GameEntity baseGame) {
        return buildDLCsForGame(1, baseGame).getFirst();
    }

}
