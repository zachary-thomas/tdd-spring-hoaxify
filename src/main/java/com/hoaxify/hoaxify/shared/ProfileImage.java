package com.hoaxify.hoaxify.shared;

import com.hoaxify.hoaxify.user.UniqueUsernameValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ProfileImageValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProfileImage {

    String message() default "{hoaxify.constraints.image.ProfileImage.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}