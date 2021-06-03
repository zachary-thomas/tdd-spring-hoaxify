package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.error.DuplicateUsernameException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    UserRepository userRepository;

    BCryptPasswordEncoder passwordEncoder;

    // Constructor injection makes it easier to test
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User save(User user){

        // Logic should not be here for validation, it is split in the controller and
        // using annotations already. Service should be for business logic.
//        if(userRepository.findByUsername(user.getUsername()) != null){
//            throw new DuplicateUsernameException();
//        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

}
