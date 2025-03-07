package ro.unibuc.hello.data.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// @Document(collection = "information")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class InformationEntity {

    @Id
    private String id;

    private String title;
    private String description;

    public InformationEntity(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
