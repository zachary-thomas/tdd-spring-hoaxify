package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.shared.GenericResponse;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import io.swagger.annotations.Api;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(SpringRunner.class)
// Integration test
// Starts the webserver
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserControllerTest {

    public static final String API_1_0_USERS = "/api/1.0/users";

    // HTTP client
    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Before
    public void cleanUp(){
        userRepository.deleteAll();
    }

    // Single assertion or requirement per test
    @Test
    public void postUser_whenUserIsValid_receiveOk(){
        // given
        User user = TestUtil.createValidUser();

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postUser_whenUserIsValid_userSavedToDatabase(){
        // given
        User user = TestUtil.createValidUser();

        // when
        postSignup(user, Object.class);

        // then
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    public void postUser_whenUserIsValid_receiveSuccessMessage(){
        // given
        User user = TestUtil.createValidUser();

        // when
        ResponseEntity<GenericResponse> response =
                postSignup(user, GenericResponse.class);

        // then
        assertThat(response.getBody().getMessage()).isNotNull();
    }

    @Test
    public void postUser_whenUserIsValid_passwordIsHashedInDatabase(){
        // given
        User user = TestUtil.createValidUser();

        // when
        postSignup(user, Object.class);

        // then
        List<User> users = userRepository.findAll();
        User inDb = users.get(0);
        assertThat(inDb.getPassword()).isNotEqualTo(user.getPassword());
    }

    @Test
    public void postUser_whenUserHasNullUsername_receiveBadRequest(){
        // given
        User user = TestUtil.createValidUser();
        user.setUsername(null);

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasNullDisplayName_receiveBadRequest(){
        // given
        User user = TestUtil.createValidUser();
        user.setDisplayName(null);

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasNullPassword_receiveBadRequest(){
        // given
        User user = TestUtil.createValidUser();
        user.setPassword(null);

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasUsernameWithLessThanRequired_receiveBadRequest(){
        // given
        User user = TestUtil.createValidUser();
        user.setUsername("abc");

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasDisplayNameWithLessThanRequired_receiveBadRequest(){
        // given
        User user = TestUtil.createValidUser();
        user.setDisplayName("abc");

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordWithLessThanRequired_receiveBadRequest(){
        // given
        User user = TestUtil.createValidUser();
        user.setPassword("P4sswd");

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasUsernameExceedsLengthLimit_receiveBadRequest(){
        // given
        User user = TestUtil.createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1,256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setUsername(valueOf256Chars);

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasDisplayNameExceedsLengthLimit_receiveBadRequest(){
        // given
        User user = TestUtil.createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1,256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setDisplayName(valueOf256Chars);

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordExceedsLengthLimit_receiveBadRequest(){
        // given
        User user = TestUtil.createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1,256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setPassword(valueOf256Chars + "A1");

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordWIthAllLowercase_receiveBadRequest(){
        // given
        User user = TestUtil.createValidUser();
        user.setPassword("alllowercase");

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordWIthAllUppercase_receiveBadRequest(){
        // given
        User user = TestUtil.createValidUser();
        user.setPassword("ALLUPPERCASE");

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordWIthAllNumber_receiveBadRequest(){
        // given
        User user = TestUtil.createValidUser();
        user.setPassword("123456789");

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserIsInvalid_receiveApiError(){
        User user = new User();

        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);

        assertThat(response.getBody().getUrl()).isEqualTo(API_1_0_USERS);
    }

    @Test
    public void postUser_whenUserIsInvalid_receiveApiErrorWithValidationErrors(){
        User user = new User();

        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);

        assertThat(response.getBody().getValidationErrors().size()).isEqualTo(3);
    }

    @Test
    public void postUser_whenUserHasNullUsername_receiveMessageOfNullErrorForUsername(){
        User user = TestUtil.createValidUser();
        user.setUsername(null);

        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);

        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("Username cannot be null");
    }

    @Test
    public void postUser_whenUserHasNullPassword_receiveMessageOfNullError(){
        User user = TestUtil.createValidUser();
        user.setPassword(null);

        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);

        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("password")).isEqualTo("Cannot be null");
    }

    @Test
    public void postUser_whenUserHasInvalidLengthUsername_receiveGenericMessageOfSizeError(){
        User user = TestUtil.createValidUser();
        user.setUsername("abc");

        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);

        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("It must have minimum 4 and maximum 255 characters");
    }

    @Test
    public void postUser_whenUserHasInvalidPasswordPattern_receiveMessageOfPasswordPatternError(){
        User user = TestUtil.createValidUser();
        user.setPassword("alllowercase");

        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);

        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("password"))
                .isEqualTo("Password must have at least one uppercase, one lowercase letter and one number");
    }

    @Test
    public void postUser_whenAnotherUserHasSameUsername_receiveBadRequest(){
        userRepository.save(TestUtil.createValidUser());
        User user = TestUtil.createValidUser();

        ResponseEntity<Object> response = postSignup(user, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenAnotherUserHasSameUsername_receiveMessageOfDuplicateUsername(){
        userRepository.save(TestUtil.createValidUser());
        User user = TestUtil.createValidUser();

        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);

        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("This name is in use");
    }

    @Test
    public void getUsers_whenThereAreNoUsersInDB_receiveOK(){
        ResponseEntity<Object> response = getUsers(new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getUsers_whenThereAreNoUsersInDB_receivePageWithZeroItems(){
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getUsers_whenThereAreUsersInDB_receivePageWithUser(){
        userRepository.save(TestUtil.createValidUser());
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    public void getUsers_whenThereAreUsersInDB_receiveUserWithoutPassword(){
        userRepository.save(TestUtil.createValidUser());
        ResponseEntity<TestPage<Map<String, Object>>> response =
                getUsers(new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {});

        Map<String, Object> entity = response.getBody().getContent().get(0);
        assertThat(entity.containsKey("password")).isFalse();
    }

    public  <T> ResponseEntity<T> postSignup(Object request, Class<T> response){
        return testRestTemplate.postForEntity(API_1_0_USERS, request, response);
    }

    public  <T> ResponseEntity<T> getUsers(ParameterizedTypeReference<T> responseType){
        return testRestTemplate.exchange(API_1_0_USERS, HttpMethod.GET, null, responseType);
    }

}
