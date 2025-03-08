package ro.unibuc.hello.data.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import ro.unibuc.hello.security.AuthenticationService;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Builder
@Document("users")
public class UserEntity {

    public enum Role {
        CUSTOMER,
        DEVELOPER
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserDetails {
        private String studio;
        private String website;

        private String firstName;
        private String lastName;

        public static UserDetails forCustomer(String firstName, String lastName) {
            return UserDetails
                    .builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .build();
        }

        public static UserDetails forDeveloper(String studio, String website) {
            return UserDetails
                    .builder()
                    .studio(studio)
                    .website(website)
                    .build();
        }
    }

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;

    @Indexed(unique = true)
    private String email;

    private Role role;

    private UserDetails details;

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

    public void setPassword(String password) {
        this.password = AuthenticationService.encryptPassword(password);
    }

    private static UserEntity buildUser(String id, String email, Role role, UserDetails details) {
        String username = role == Role.CUSTOMER
                ? String.format("%s%s", details.getFirstName(), details.getLastName())
                : details.getStudio().replaceAll("\\s+", "");

        return UserEntity
                .builder()
                .id(id)
                .username(username)
                .password(String.format("%s1234", username))
                .email(email)
                .role(role)
                .details(details)
                .build();
    }

    public static UserEntity buildDeveloper(String id, String email, String studio, String website) {
        return buildUser(
                id,
                email,
                Role.DEVELOPER,
                UserDetails.forDeveloper(studio, website)
        );
    }

    public static UserEntity buildCustomer(String id, String email, String firstName, String lastName) {
        return buildUser(
                id,
                email,
                Role.CUSTOMER,
                UserDetails.forCustomer(firstName, lastName)
        );
    }
}
