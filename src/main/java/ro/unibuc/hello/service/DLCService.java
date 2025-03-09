package ro.unibuc.hello.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.DeveloperOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.dto.Game;

import static ro.unibuc.hello.utils.ResponseUtils.*;

@Service
public class DLCService extends GameService {

    @Override
    protected GameEntity.Type getType() {
        return GameEntity.Type.DLC;
    }

    @DeveloperOnly
    public ResponseEntity<?> createDLC(String baseGameId, Game dlcInput) {
        GameEntity baseGame = gameRepository.findByIdAndType(baseGameId, GameEntity.Type.GAME);
        if (baseGame == null) return badRequest("No game found at id %s", baseGameId);

        dlcInput.setBaseGame(baseGame);
        ResponseEntity<?> dlcBody = createGame(dlcInput);

        if (dlcBody.getBody() instanceof GameEntity dlc) {
            baseGame.getDlcs().add(dlc);
            gameRepository.save(baseGame);
        }

        return dlcBody;
    }
}
