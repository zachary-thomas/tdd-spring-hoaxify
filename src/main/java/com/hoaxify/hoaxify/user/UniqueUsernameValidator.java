package com.hoaxify.hoaxify.user;

import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    @Autowired
    UserRepository userRepository;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {

        if(userRepository.findByUsername(value) == null){
            return true;
        }
        return false;
    }
}
