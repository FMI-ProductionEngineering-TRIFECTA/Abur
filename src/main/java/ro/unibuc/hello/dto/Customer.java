package ro.unibuc.hello.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer extends User {

    private String firstName;

    private String lastName;

}
