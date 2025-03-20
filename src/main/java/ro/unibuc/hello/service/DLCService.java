package ro.unibuc.hello.service;

import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.DeveloperOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.dto.Game;

import static ro.unibuc.hello.data.entity.GameEntity.Type;

@Service
public class DLCService extends GameService {

    private final GameService gameService;

    public DLCService(GameService gameService) {
        super();
        this.gameService = gameService;
    }

    @Override
    protected Type getType() {
        return Type.DLC;
    }

    @DeveloperOnly
    public GameEntity createDLC(String baseGameId, Game dlcInput) {
        GameEntity baseGame = gameService.getGame(baseGameId);
        dlcInput.setBaseGame(baseGame);

        GameEntity dlc = createGame(dlcInput);
        baseGame.getDlcs().add(dlc);
        gameRepository.save(baseGame);

        return dlc;
    }

}
