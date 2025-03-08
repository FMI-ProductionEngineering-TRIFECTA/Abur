package ro.unibuc.hello.service;

import ro.unibuc.hello.data.entity.GameEntity;

public class DLCService extends GameService {

    @Override
    protected GameEntity.Type getType() {
        return GameEntity.Type.DLC;
    }

}
