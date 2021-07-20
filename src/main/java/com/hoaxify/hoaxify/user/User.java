package com.hoaxify.hoaxify.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import com.hoaxify.hoaxify.hoax.Hoax;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collection;

import java.beans.Transient;
import java.util.List;

// Getters, setters, constructor,
// equals, hashcode, toString
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@Table(uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class User implements UserDetails {

    @Id
    @GeneratedValue
    // JsonView is just an example
    // we use UserVM as a better solution
    // to only send those fields
    //@JsonView(Views.Base.class)
    private long id;

    // moved to Validation.Messages.properties file
    @NotNull(message = "{hoaxify.constraints.username.NotNull.message}")
    //@NotNull(message = "Username cannot be null")
    @Size(min = 4, max = 255)
    @UniqueUsername
    //@JsonView(Views.Base.class)
    private String username;

    @NotNull
    @Size(min = 4, max = 255)
    //@JsonView(Views.Base.class)
    private String displayName;

    @NotNull
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "{hoaxify.constraints.password.Pattern.message}")
    // Won't work in our use case because it gets rid of receive and response
    // Use JsonView to just remove in response.
    //@JsonIgnore
    private String password;

    //@JsonView(Views.Base.class)
    private String image;

    @Override
    // Not in db
    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("Role_User");
    }

    @OneToMany(mappedBy = "user")
    // fetch = FetchType.EAGER
    private List<Hoax> hoaxes;

    @Override
    @Transient
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @Transient
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isEnabled() {
        return true;
    }
}
