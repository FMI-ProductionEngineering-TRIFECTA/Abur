package ro.unibuc.hello.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Developer extends User {

    private String studio;
    private String website;

}
