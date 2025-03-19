package ro.unibuc.hello.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder()
public class Customer extends User {

    private String firstName;

    private String lastName;

}
