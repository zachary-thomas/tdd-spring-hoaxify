package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.configuration.AppConfiguration;
import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.shared.GenericResponse;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import com.hoaxify.hoaxify.user.UserService;
import com.hoaxify.hoaxify.user.vm.UserUpdateVM;
import com.hoaxify.hoaxify.user.vm.UserVM;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
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

    @Autowired
    UserService userService;

    @Autowired
    AppConfiguration appConfiguration;

    @Before
    public void cleanUp() {
        userRepository.deleteAll();

        // there must be this interceptor clear part so that each test will start with
        // non authenticated testRestTemplate this is missing. I couldn't find where we
        // add this but, it must be added after a fail we see running all tests in one of the previous sections
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    @After
    public void cleanDirectory() throws IOException {
        FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    // Single assertion or requirement per test
    @Test
    public void postUser_whenUserIsValid_receiveOk() {
        // given
        User user = TestUtil.createValidUser();

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postUser_whenUserIsValid_userSavedToDatabase() {
        // given
        User user = TestUtil.createValidUser();

        // when
        postSignup(user, Object.class);

        // then
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    public void postUser_whenUserIsValid_receiveSuccessMessage() {
        // given
        User user = TestUtil.createValidUser();

        // when
        ResponseEntity<GenericResponse> response =
                postSignup(user, GenericResponse.class);

        // then
        assertThat(response.getBody().getMessage()).isNotNull();
    }

    @Test
    public void postUser_whenUserIsValid_passwordIsHashedInDatabase() {
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
    public void postUser_whenUserHasNullUsername_receiveBadRequest() {
        // given
        User user = TestUtil.createValidUser();
        user.setUsername(null);

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasNullDisplayName_receiveBadRequest() {
        // given
        User user = TestUtil.createValidUser();
        user.setDisplayName(null);

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasNullPassword_receiveBadRequest() {
        // given
        User user = TestUtil.createValidUser();
        user.setPassword(null);

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasUsernameWithLessThanRequired_receiveBadRequest() {
        // given
        User user = TestUtil.createValidUser();
        user.setUsername("abc");

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasDisplayNameWithLessThanRequired_receiveBadRequest() {
        // given
        User user = TestUtil.createValidUser();
        user.setDisplayName("abc");

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordWithLessThanRequired_receiveBadRequest() {
        // given
        User user = TestUtil.createValidUser();
        user.setPassword("P4sswd");

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasUsernameExceedsLengthLimit_receiveBadRequest() {
        // given
        User user = TestUtil.createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setUsername(valueOf256Chars);

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasDisplayNameExceedsLengthLimit_receiveBadRequest() {
        // given
        User user = TestUtil.createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setDisplayName(valueOf256Chars);

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordExceedsLengthLimit_receiveBadRequest() {
        // given
        User user = TestUtil.createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setPassword(valueOf256Chars + "A1");

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordWIthAllLowercase_receiveBadRequest() {
        // given
        User user = TestUtil.createValidUser();
        user.setPassword("alllowercase");

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordWIthAllUppercase_receiveBadRequest() {
        // given
        User user = TestUtil.createValidUser();
        user.setPassword("ALLUPPERCASE");

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordWIthAllNumber_receiveBadRequest() {
        // given
        User user = TestUtil.createValidUser();
        user.setPassword("123456789");

        // when
        ResponseEntity<Object> response = postSignup(user, Object.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserIsInvalid_receiveApiError() {
        User user = new User();

        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);

        assertThat(response.getBody().getUrl()).isEqualTo(API_1_0_USERS);
    }

    @Test
    public void postUser_whenUserIsInvalid_receiveApiErrorWithValidationErrors() {
        User user = new User();

        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);

        assertThat(response.getBody().getValidationErrors().size()).isEqualTo(3);
    }

    @Test
    public void postUser_whenUserHasNullUsername_receiveMessageOfNullErrorForUsername() {
        User user = TestUtil.createValidUser();
        user.setUsername(null);

        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);

        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("Username cannot be null");
    }

    @Test
    public void postUser_whenUserHasNullPassword_receiveMessageOfNullError() {
        User user = TestUtil.createValidUser();
        user.setPassword(null);

        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);

        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("password")).isEqualTo("Cannot be null");
    }

    @Test
    public void postUser_whenUserHasInvalidLengthUsername_receiveGenericMessageOfSizeError() {
        User user = TestUtil.createValidUser();
        user.setUsername("abc");

        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);

        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("It must have minimum 4 and maximum 255 characters");
    }

    @Test
    public void postUser_whenUserHasInvalidPasswordPattern_receiveMessageOfPasswordPatternError() {
        User user = TestUtil.createValidUser();
        user.setPassword("alllowercase");

        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);

        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("password"))
                .isEqualTo("Password must have at least one uppercase, one lowercase letter and one number");
    }

    @Test
    public void postUser_whenAnotherUserHasSameUsername_receiveBadRequest() {
        userRepository.save(TestUtil.createValidUser());
        User user = TestUtil.createValidUser();

        ResponseEntity<Object> response = postSignup(user, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenAnotherUserHasSameUsername_receiveMessageOfDuplicateUsername() {
        userRepository.save(TestUtil.createValidUser());
        User user = TestUtil.createValidUser();

        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);

        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("This name is in use");
    }

    @Test
    public void getUsers_whenThereAreNoUsersInDB_receiveOK() {
        ResponseEntity<Object> response = getUsers(new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getUsers_whenThereAreNoUsersInDB_receivePageWithZeroItems() {
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getUsers_whenThereAreUsersInDB_receivePageWithUser() {
        userRepository.save(TestUtil.createValidUser());
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    public void getUsers_whenThereAreUsersInDB_receiveUserWithoutPassword() {
        userRepository.save(TestUtil.createValidUser());
        ResponseEntity<TestPage<Map<String, Object>>> response =
                getUsers(new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {
                });

        Map<String, Object> entity = response.getBody().getContent().get(0);
        assertThat(entity.containsKey("password")).isFalse();
    }

    @Test
    public void getUsers_whenPageRequests3ItemsPerPageWhereDatabaseHas20Users_receive3Users() {
        IntStream.rangeClosed(1, 20).mapToObj(i -> "test-user-" + i)
                .map(TestUtil::createValidUser)
                .forEach(userRepository::save);
        String path = API_1_0_USERS + "?page=0&size=3";

        ResponseEntity<TestPage<Object>> response =
                getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
                });

        assertThat(response.getBody().getContent().size()).isEqualTo(3);
    }

    @Test
    public void getUsers_whenPageSizeNotProvided_receivePageSizeAs10() {
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getSize()).isEqualTo(10);
    }

    @Test
    public void getUsers_whenPageSizeGreaterThan100_receivePageSizeAs100() {
        IntStream.rangeClosed(1, 500).mapToObj(i -> "test-user-" + i)
                .map(TestUtil::createValidUser)
                .forEach(userRepository::save);
        String path = API_1_0_USERS + "?size=500";

        ResponseEntity<TestPage<Object>> response =
                getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
                });

        assertThat(response.getBody().getContent().size()).isEqualTo(100);
    }

    @Test
    public void getUsers_whenPageSizeIsNegative_receivePageSizeAs10() {
        IntStream.rangeClosed(1, 20).mapToObj(i -> "test-user-" + i)
                .map(TestUtil::createValidUser)
                .forEach(userRepository::save);
        String path = API_1_0_USERS + "?size=-5";

        ResponseEntity<TestPage<Object>> response =
                getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
                });

        assertThat(response.getBody().getContent().size()).isEqualTo(10);
    }

    @Test
    public void getUsers_whenPageIsNegative_receiveFirstPage() {
        String path = API_1_0_USERS + "?page=-5";

        ResponseEntity<TestPage<Object>> response =
                getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
                });

        assertThat(response.getBody().getNumber()).isEqualTo(0);
    }

    @Test
    public void getUsers_whenUserLoggedIn_receivePageWIthoutLoggedInUser() {
        userService.save(TestUtil.createValidUser("user1"));
        userService.save(TestUtil.createValidUser("user2"));
        userService.save(TestUtil.createValidUser("user3"));
        authenticate("user1");

        ResponseEntity<TestPage<Object>> response =
                getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
                });

        assertThat(response.getBody().getTotalElements()).isEqualTo(2);
    }

    @Test
    public void getUserByUsername_whenUserExist_receiveOk() {
        String username = "test-user";
        userService.save(TestUtil.createValidUser(username));

        ResponseEntity<Object> response = getUser(username, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getUserByUsername_whenUserExist_receiveUserWithoutPassword() {
        String username = "test-user";
        userService.save(TestUtil.createValidUser(username));

        ResponseEntity<String> response = getUser(username, String.class);

        assertThat(response.getBody().contains("password")).isFalse();
    }

    @Test
    public void getUserByUsername_whenUserDoesNotExist_receiveUserNotFound() {
        ResponseEntity<Object> response = getUser("unknown-user", Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getUserByUsername_whenUserDoesNotExist_receiveApiError() {
        ResponseEntity<ApiError> response = getUser("unknown-user", ApiError.class);

        assertThat(response.getBody().getMessage().contains("unknown-user")).isTrue();
    }

    @Test
    public void putUser_whenUnauthorizedUserSendsTheRequest_receiveUnauthorized() {
        ResponseEntity<Object> response = putUser(123, null, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void putUser_whenUnauthorizedUserSendsUpdateForAnotherUser_receiveForbidden() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());

        long anotherUserId = user.getId() + 123;

        ResponseEntity<Object> response = putUser(anotherUserId, null, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void putUser_whenUnauthorizedUserSendsTheRequest_receiveApiError() {
        ResponseEntity<ApiError> response = putUser(123, null, ApiError.class);

        assertThat(response.getBody().getUrl()).contains("users/123");
    }

    @Test
    public void putUser_whenUnauthorizedUserSendsUpdateForAnotherUser_receiveApiError() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());

        long anotherUserId = user.getId() + 123;

        ResponseEntity<ApiError> response = putUser(anotherUserId, null, ApiError.class);

        assertThat(response.getBody().getUrl()).contains("users/" + anotherUserId);
    }

    @Test
    public void putUser_whenValidRequestBodyFromAuthorizedUser_receiveOk() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void putUser_whenValidRequestBodyFromAuthorizedUser_displayNameUpdated() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        putUser(user.getId(), requestEntity, Object.class);

        User inDb = userRepository.findByUsername("user1");
        assertThat(inDb.getDisplayName()).isEqualTo(updatedUser.getDisplayName());
    }

    @Test
    public void putUser_whenValidRequestBodyFromAuthorizedUser_receiveUserVMWIthUpdatedDisplayName() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        assertThat(response.getBody().getDisplayName()).isEqualTo(updatedUser.getDisplayName());
    }

    // These fail due to a change in the UserService update method
    @Test
    public void putUser_withValidRequestBodyWithSupportedImageFromAuthUser_receiveUserVMwithRandomImageName() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();
        String imageString = readFileToBase64("profile.png");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        assertThat(response.getBody().getImage()).isNotEqualTo("profile-image.png");
    }

    @Test
    public void putUser_withValidRequestBodyWithSupportedImageFromAuthUser_imageIsStoredUnderProfileFolder() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();
        String imageString = readFileToBase64("profile.png");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);
        String storedImageName = response.getBody().getImage();
        String profilePicturePath = appConfiguration.getFullProfileImagesPath() + "/" + storedImageName;
        File storedImage = new File(profilePicturePath);

        assertThat(storedImage.exists()).isTrue();
    }

    @Test
    public void putUser_withInvalidRequestBodyWithNullDisplayNameFromAuthUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = new UserUpdateVM();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void putUser_withInvalidRequestBodyWithLessThanMinDisplayNameFromAuthUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = new UserUpdateVM();
        updatedUser.setDisplayName("abc");

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void putUser_withInvalidRequestBodyWithMoreThanMaxDisplayNameFromAuthUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = new UserUpdateVM();
        updatedUser.setDisplayName(IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining()));

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void putUser_withValidRequestBodyWithSupportedJPGImageFromAuthUser_receiveOk() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();
        String imageString = readFileToBase64("test-jpg.jpg");

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void putUser_withValidRequestBodyWithGIFImageFromAuthUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();
        String imageString = readFileToBase64("test-gif.gif");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void putUser_withValidRequestBodyWithTXTImageFromAuthUser_receiveValidationErrorForProfileImage() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();
        String imageString = readFileToBase64("test-txt.txt");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<ApiError> response = putUser(user.getId(), requestEntity, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();

        assertThat(validationErrors.get("image")).isEqualTo("Only PNG and JPG files are allowed");
    }

    @Test
    public void putUser_withValidRequestBodyWithSupportedJPGImageForUserWithImage_removesOldImage() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();
        String imageString = readFileToBase64("test-jpg.jpg");

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        putUser(user.getId(), requestEntity, UserVM.class);

        String storedImageName = response.getBody().getImage();
        String profilePicturePath = appConfiguration.getFullProfileImagesPath() + "/" + storedImageName;
        File storedImage = new File(profilePicturePath);
        assertThat(storedImage.exists()).isFalse();
    }

    private String readFileToBase64(String fileName) throws IOException {
        ClassPathResource imageResource = new ClassPathResource(fileName);
        byte[] imageArr = FileUtils.readFileToByteArray(imageResource.getFile());
        return Base64.getEncoder().encodeToString(imageArr);
    }

    private UserUpdateVM createValidUserUpdateVM() {
        UserUpdateVM updatedUser = new UserUpdateVM();
        updatedUser.setDisplayName("newDisplayName");
        return updatedUser;
    }

    public <T> ResponseEntity<T> putUser(long id, HttpEntity<?> requestEntity, Class<T> responseType) {
        String path = API_1_0_USERS + "/" + id;
        return testRestTemplate.exchange(path, HttpMethod.PUT, requestEntity, responseType);
    }

    public <T> ResponseEntity<T> getUser(String username, Class<T> responseType) {
        String path = API_1_0_USERS + "/" + username;
        return testRestTemplate.getForEntity(path, responseType);
    }

    public <T> ResponseEntity<T> postSignup(Object request, Class<T> response) {
        return testRestTemplate.postForEntity(API_1_0_USERS, request, response);
    }

    public <T> ResponseEntity<T> getUsers(ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(API_1_0_USERS, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getUsers(String path, ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private void authenticate(String username) {
        testRestTemplate.getRestTemplate().getInterceptors()
                .add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }

}
