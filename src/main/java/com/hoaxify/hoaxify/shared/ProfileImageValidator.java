package com.hoaxify.hoaxify.shared;

import com.hoaxify.hoaxify.file.FileService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Base64;

public class ProfileImageValidator implements ConstraintValidator<ProfileImage, String> {

    @Autowired
    FileService fileService;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if(value == null){
            return true;
        }

        byte[] decodeBytes = Base64.getDecoder().decode(value);
        String fileType = fileService.detectType(decodeBytes);

        if(fileType.equalsIgnoreCase("image/png")
                || fileType.equalsIgnoreCase("image/jpeg") ){
            return true;
        }

        return false;
    }
}
