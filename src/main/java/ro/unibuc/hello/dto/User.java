package ro.unibuc.hello.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder()
public abstract class User {

    private String username;
    private String password;
    private String email;

}
