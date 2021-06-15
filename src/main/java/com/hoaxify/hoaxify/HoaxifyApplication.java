package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.stream.IntStream;

// exclude security for now, until implementation is ready
@SpringBootApplication
//(exclude = SecurityAutoConfiguration.class)
public class HoaxifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(HoaxifyApplication.class, args);
    }

    @Bean
    @Profile("!test")
        // Executed after application is initialized
        // not for tests.
    CommandLineRunner run(UserService userService) {
        return (args) -> {
            IntStream.rangeClosed(1, 15)
                    .mapToObj(i -> {
                        return User.builder()
                                .displayName("display" + i)
                                .username("user" + i)
                                .password("P4ssword")
                                .build();
                    }).forEach(userService::save);
        };
    }

}


