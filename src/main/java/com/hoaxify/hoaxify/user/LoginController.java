package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.error.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @PostMapping("/api/1.0/login")
    void handleLogin(){

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
