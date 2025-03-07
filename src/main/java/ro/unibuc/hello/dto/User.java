package ro.unibuc.hello.dto;

import lombok.Getter;

@Getter
public abstract class User {

    private String username;
    private String password;
    private String email;

}
