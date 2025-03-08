package ro.unibuc.hello.service;

import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.entity.UserEntity;
import ro.unibuc.hello.dto.Developer;

@Service
public class DeveloperService extends UserService<Developer> {

    @Override
    protected UserEntity.Role getRole() {
        return UserEntity.Role.DEVELOPER;
    }

}
