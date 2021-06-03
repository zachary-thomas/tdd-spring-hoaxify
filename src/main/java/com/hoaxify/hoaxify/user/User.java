package com.hoaxify.hoaxify.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

// Getters, setters, constructor,
// equals, hashcode, toString
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@Table(uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class User {

    @Id
    @GeneratedValue
    private long id;

    // moved to Validation.Messages.properties file
    @NotNull(message = "{hoaxify.constraints.username.NotNull.message}")
    //@NotNull(message = "Username cannot be null")
    @Size(min = 4, max = 255)
    @UniqueUsername
    private String username;

    @NotNull
    @Size(min = 4, max = 255)
    private String displayName;

    @NotNull
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "{hoaxify.constraints.password.Pattern.message}")
    private String password;
}
