package ro.unibuc.hello.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder()
@AllArgsConstructor
@NoArgsConstructor
public class Customer extends User {

    private String firstName;
    private String lastName;

}
