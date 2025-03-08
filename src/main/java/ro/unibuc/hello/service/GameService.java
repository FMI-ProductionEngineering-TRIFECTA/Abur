package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.repository.GameRepository;

import java.util.List;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    public GameEntity getGameById(String id) {
        return gameRepository.findByIdAndType(id, GameEntity.Type.GAME);
    }

    public List<GameEntity> getAllGames() {
        return gameRepository.findByType(GameEntity.Type.GAME);
    }
}
