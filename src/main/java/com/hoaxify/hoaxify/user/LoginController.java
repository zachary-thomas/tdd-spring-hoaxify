package com.hoaxify.hoaxify.user;

import com.fasterxml.jackson.annotation.JsonView;
import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.user.vm.UserVM;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @PostMapping("/api/1.0/login")
        // We don't need JsonView because of the user vm/dto converter to remove the password
    //@JsonView(Views.Base.class)
    UserVM handleLogin(@CurrentUser User loggedInUser){
        //User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new UserVM(loggedInUser);
    }

    // Will not hit because spring security handles before the controller
    // is hit in the filter chain.
    // Use the ErrorHandler that implements ErrorController
//    @ExceptionHandler({AccessDeniedException.class})
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    ApiError handleAccessDeniedException(){
//        return new ApiError(HttpStatus.UNAUTHORIZED.value(), "Access Error", "/api/1.0/login");
//    }
}
