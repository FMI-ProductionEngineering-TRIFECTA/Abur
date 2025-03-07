package ro.unibuc.hello.data.entity;

import org.springframework.data.annotation.Id;
import ro.unibuc.hello.security.AuthenticationService;

public class UserEntity {

    public enum Role {
        CUSTOMER,
        DEVELOPER
    }

    public static class UserDetails {
        private String studio;
        private String website;

        private String firstName;
        private String lastName;

        private UserDetails(String firstName, String lastName, String studio, String website) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.studio = studio;
            this.website = website;
        }

        public static UserDetails forCustomer(String firstName, String lastName) {
            return new UserDetails(firstName, lastName, null, null);
        }

        public static UserDetails forDeveloper(String studio, String website) {
            return new UserDetails(null, null, studio, website);
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
    }

    @Id
    private String id;

    private String username;
    private String password;
    private String email;
    private Role role;

    private UserDetails details;

    public UserEntity() {}

    public UserEntity(String username, String password, String email, Role role, UserDetails details) {
        this.username = username;
        this.password = AuthenticationService.encryptPassword(password);
        this.email = email;
        this.role = role;
        this.details = details;
    }

    public UserEntity(String id, String username, String password, String email, Role role, UserDetails details) {
        this.id = id;
        this.username = username;
        this.password = AuthenticationService.encryptPassword(password);
        this.email = email;
        this.role = role;
        this.details = details;
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
        this.password = AuthenticationService.encryptPassword(password);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserDetails getDetails() {
        return details;
    }

    public void setDetails(UserDetails details) {
        this.details = details;
    }

}
