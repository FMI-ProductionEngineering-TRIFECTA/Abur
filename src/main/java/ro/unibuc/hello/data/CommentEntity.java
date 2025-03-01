package ro.unibuc.hello.data;

import org.springframework.data.annotation.Id;

public class CommentEntity {

    @Id
    private String id;

    private String description;

    public CommentEntity() {}

    public CommentEntity(String description) {
        this.description = description;
    }

    public CommentEntity(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format(
                "Comment[id='%s', description='%s']",
                id, description);
    }
}
