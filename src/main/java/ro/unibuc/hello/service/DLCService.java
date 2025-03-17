package ro.unibuc.hello.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.DeveloperOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.dto.Game;

import static ro.unibuc.hello.data.entity.GameEntity.*;

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
    public ResponseEntity<?> createDLC(String baseGameId, Game dlcInput) {
        GameEntity baseGame = gameService.getGame(baseGameId);
        dlcInput.setBaseGame(baseGame);
        ResponseEntity<?> dlcBody = createGame(dlcInput);

        if (dlcBody.getBody() instanceof GameEntity dlc) {
            baseGame.getDlcs().add(dlc);
            gameRepository.save(baseGame);
        }

        return dlcBody;
    }

}
