package ro.unibuc.hello.utils;

import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;

import java.util.List;

public final class DLCTestUtils {

    private DLCTestUtils() {}

    public static GameEntity buildBaseGame() {
        GameEntity baseGame = GameTestUtils.buildGame();
        baseGame.setId("baseGameID");
        return baseGame;
    }

    public static GameEntity buildBaseGame(UserEntity developer) {
        GameEntity baseGame = buildBaseGame();
        baseGame.setDeveloper(developer);
        return baseGame;
    }

    public static List<GameEntity> buildDLCsForGame(int total, GameEntity baseGame) {
        List<GameEntity> dlcs = GameTestUtils.buildDLCsForGame(total, baseGame);
        for (int i = 0; i < total; i++) {
            dlcs.get(i).setId(String.valueOf(i + 1));
        }
        return dlcs;
    }

    public static GameEntity buildDLCForGame(GameEntity baseGame) {
        return buildDLCsForGame(1, baseGame).getFirst();
    }

}
