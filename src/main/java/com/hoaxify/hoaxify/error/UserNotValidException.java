package com.hoaxify.hoaxify.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// What status to send when exception is thrown
// not used, just for an example
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserNotValidException extends RuntimeException {

}
