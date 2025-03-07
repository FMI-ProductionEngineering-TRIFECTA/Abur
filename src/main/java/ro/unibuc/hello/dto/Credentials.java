package ro.unibuc.hello.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class Credentials {

    @NotNull
    private String username;

    @NotNull
    private String password;

}
