package ro.unibuc.hello.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@EqualsAndHashCode
@SuperBuilder()
@AllArgsConstructor
@NoArgsConstructor
public abstract class User {

    private String username;
    private String password;
    private String email;

}
