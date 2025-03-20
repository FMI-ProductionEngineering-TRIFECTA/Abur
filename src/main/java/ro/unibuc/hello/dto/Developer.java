package ro.unibuc.hello.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder()
public class Developer extends User {

    private String studio;
    private String website;

}
