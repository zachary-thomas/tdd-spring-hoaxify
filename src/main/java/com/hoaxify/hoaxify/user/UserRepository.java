package com.hoaxify.hoaxify.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByUsernameContaining(String username);
    User findByUsernameAndDisplayName(String username, String displayName);
    User findByUsername(String username);

    // Find all is taken, so we have to write a query.
    // We also use another method other than Views to return
    // the user object without the password field.
//    @Query(value = "SELECT * FROM user", nativeQuery = true)
//    Page<UserProjection> getAllUsersProjection(Pageable pageable);
}
