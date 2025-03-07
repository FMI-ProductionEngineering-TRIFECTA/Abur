package ro.unibuc.hello.data.entity;

import org.springframework.data.annotation.Id;
import ro.unibuc.hello.security.AuthenticationService;

public class CustomerEntity {

    @Id
    private String id;

    private String username;
    private String password;
    private String email;

    private String firstName;
    private String lastName;

    public CustomerEntity() {}

    public CustomerEntity(String username, String password, String email, String firstName, String lastName) {
        this.username = username;
        this.password = AuthenticationService.encryptPassword(password);
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public CustomerEntity(String id, String username, String password, String email, String firstName, String lastName) {
        this.id = id;
        this.username = username;
        this.password = AuthenticationService.encryptPassword(password);
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "CustomerEntity{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

}
