package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
// Provides clean database each time
@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    UserRepository userRepository;

    @Test
    public void findByUsername_whenUserExists_returnUser(){
        User user = User.builder()
                .username("test-user")
                .displayName("test-display")
                .password("P4ssword")
                .build();

        testEntityManager.persist(user);

        User inDb = userRepository.findByUsername("test-user");
        assertThat(inDb).isNotNull();
    }

    @Test
    public void findByUsername_whenUserDoesNotExist_returnNull(){
        User inDb = userRepository.findByUsername("notExistingUser");
        assertThat(inDb).isNull();
    }
}