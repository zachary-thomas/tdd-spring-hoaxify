package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.shared.GenericResponse;
import com.hoaxify.hoaxify.user.vm.UserVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;

// Methods to handle http requests
@RestController
@RequestMapping("/api/1.0")
public class UserController {

    // Field injection for controllers
    // controller doesn't need unit tests
    @Autowired
    UserService userService;

    //@PostMapping("/api/1.0/users")
    @PostMapping("/users")
    GenericResponse createUser(@Valid @RequestBody User user) {

        // Don't need validations with @Valid annotation and
        // with bean validations in the class, ie @NotNull
//        if(user.getUsername() == null || user.getDisplayName() == null){
//            throw new UserNotValidException();
//        }
        userService.save(user);
        return new GenericResponse("User saved");
    }

    @GetMapping("/users")
        //@JsonView(Views.Base.class)
//    Page<UserVM> getUsers(@RequestParam(required = false, defaultValue = "0") int currentPage,
//                          @RequestParam(required = false, defaultValue = "20") int pageSize) {

    Page<UserVM> getUsers(@CurrentUser User loggedInUser, Pageable pageable) {
        // Convert using the user dto
        return userService.getUsers(loggedInUser, pageable).map((user) -> new UserVM(user));
    }

    @GetMapping("/users/{username}")
    UserVM getUserByName(@PathVariable String username){
        return new UserVM(userService.getByUsername(username));
    }

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
