package ro.unibuc.hello.dto;

import jakarta.validation.constraints.NotNull;

public class LoginInput {

    @NotNull
    private String username;

    @NotNull
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
