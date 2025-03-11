package ro.unibuc.hello.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.annotation.DeveloperOnly;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.dto.Game;
import ro.unibuc.hello.exception.ValidationException;

import static ro.unibuc.hello.data.entity.GameEntity.*;

@Service
public class DLCService extends GameService {

    @Override
    protected Type getType() {
        return Type.DLC;
    }

    @DeveloperOnly
    public ResponseEntity<?> createDLC(String baseGameId, Game dlcInput) {
        GameEntity baseGame = gameRepository.findByIdAndType(baseGameId, Type.GAME);
        if (baseGame == null) throw new ValidationException("No game found at id %s", baseGameId);

        dlcInput.setBaseGame(baseGame);
        ResponseEntity<?> dlcBody = createGame(dlcInput);

        if (dlcBody.getBody() instanceof GameEntity dlc) {
            baseGame.getDlcs().add(dlc);
            gameRepository.save(baseGame);
        }

        return dlcBody;
    }

}
