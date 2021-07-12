package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.error.NotFoundException;
import com.hoaxify.hoaxify.file.FileService;
import com.hoaxify.hoaxify.user.vm.UserUpdateVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class UserService {

    UserRepository userRepository;

    PasswordEncoder passwordEncoder;

    FileService fileService;

    // Constructor injection makes it easier to test
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       FileService fileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
    }

    public User save(User user) {

        // Logic should not be here for validation, it is split in the controller and
        // using annotations already. Service should be for business logic.
//        if(userRepository.findByUsername(user.getUsername()) != null){
//            throw new DuplicateUsernameException();
//        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Page<User> getUsers(User loggedInUser, Pageable pageable) {
        //Pageable pageable = PageRequest.of(currentPage, pageSize);
        if (loggedInUser != null) {
            return userRepository.findByUsernameNot(loggedInUser.getUsername(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    public User getByUsername(String username) {
        User inDb = userRepository.findByUsername(username);

        if (inDb == null) {
            throw new NotFoundException(username + " not found");
        }
        return inDb;
    }

    public User update(long id, UserUpdateVM userUpdateVM) {
        User inDb = userRepository.getOne(id);
        inDb.setDisplayName(userUpdateVM.getDisplayName());

        if(userUpdateVM.getImage() != null){
            String savedImageName = null;

            try {
                savedImageName = fileService.saveProfileImage(userUpdateVM.getImage());
                fileService.deleteProfileImage(inDb.getImage());
                inDb.setImage(savedImageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return userRepository.save(inDb);
    }
}
