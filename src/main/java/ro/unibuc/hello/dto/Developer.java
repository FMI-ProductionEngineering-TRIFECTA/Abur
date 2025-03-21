package ro.unibuc.hello.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder()
@AllArgsConstructor
@NoArgsConstructor
public class Developer extends User {

    private String studio;
    private String website;

}
