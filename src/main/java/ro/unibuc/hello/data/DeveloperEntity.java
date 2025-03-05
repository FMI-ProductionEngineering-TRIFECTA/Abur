package ro.unibuc.hello.data;

import org.springframework.data.annotation.Id;

public class DeveloperEntity {

    @Id
    private String id;

    private String username;
    private String password;
    private String email;

    private String studio;
    private String website;

    public DeveloperEntity(String username, String password, String email, String studio, String website) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.studio = studio;
        this.website = website;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public String toString() {
        return "DeveloperEntity{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", studio='" + studio + '\'' +
                ", website='" + website + '\'' +
                '}';
    }

}
