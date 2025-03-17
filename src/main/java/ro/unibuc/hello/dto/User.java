package ro.unibuc.hello.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class User {

    private String username;
    private String password;
    private String email;

}
