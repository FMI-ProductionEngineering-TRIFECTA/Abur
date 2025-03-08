package ro.unibuc.hello.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class Credentials {

    private String username;

    private String password;

}
