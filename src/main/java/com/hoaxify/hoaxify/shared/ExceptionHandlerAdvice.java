package com.hoaxify.hoaxify.shared;

import com.hoaxify.hoaxify.error.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ApiError apiError = new ApiError(400, "Validation error", request.getServletPath());

        // Message errors come from binding result object
        //BindingResult result = exception.getBindingResult();

        apiError.setValidationErrors(new HashMap<>());

        exception.getBindingResult().getFieldErrors().forEach(x -> {
            apiError.getValidationErrors().put(x.getField(), x.getDefaultMessage());
        });

        return apiError;
    }

}
