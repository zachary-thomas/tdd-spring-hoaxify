package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.hoax.Hoax;
import com.hoaxify.hoaxify.hoax.HoaxRepository;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import com.hoaxify.hoaxify.user.UserService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HoaxControllerTest {

    public static final String API_1_0_HOAXES = "/api/1.0/hoaxes";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    HoaxRepository hoaxRepository;

    @Before
    public void cleanUp() {
        hoaxRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

//    @After
//    public void cleanupAfter(){
//        // Prevent stale data
//        hoaxRepository.deleteAll();
//    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_receiveOk(){
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();

        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsUnAuthorized_receiveUnauthorized(){
        Hoax hoax = TestUtil.createValidHoax();

        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsUnAuthorized_receiveApiError(){
        Hoax hoax = TestUtil.createValidHoax();

        ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);

        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxSavedToDB(){
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();

        postHoax(hoax, Object.class);

        assertThat(hoaxRepository.count()).isEqualTo(1);
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxSavedToDBWithTimestamp(){
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();

        postHoax(hoax, Object.class);

        Hoax inDb = hoaxRepository.findAll().get(0);

        assertThat(inDb.getTimestamp()).isNotNull();
    }

    @Test
    public void postHoax_whenHoaxContentIsNullAndUserIsAuthorized_receiveBadRequest(){
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();

        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postHoax_whenHoaxContentIsLessThan10CharsAndUserIsAuthorized_receiveBadRequest(){
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();
        hoax.setContent("123456789");

        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postHoax_whenHoaxContentIs5000CharsAndUserIsAuthorized_receiveOk(){
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();
        hoax.setContent(IntStream.rangeClosed(1, 5000).mapToObj(i -> "x").collect(Collectors.joining()));

        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postHoax_whenHoaxContentIsMoreThan5000CharsAndUserIsAuthorized_receiveBadRequest(){
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();
        hoax.setContent(IntStream.rangeClosed(1, 5001).mapToObj(i -> "x").collect(Collectors.joining()));

        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postHoax_whenHoaxContentIsNullAndUserIsAuthorized_receiveApiErrorWithValidationErrors(){
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();

        ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);

        assertThat(response.getBody().getValidationErrors().get("content")).isNotNull();
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxSavedWithAuthenticatedUserInfo(){
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();

        postHoax(hoax, Object.class);

        Hoax inDb = hoaxRepository.findAll().get(0);

        assertThat(inDb.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxCanBeAccessedFromUserEntity(){
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();

        postHoax(hoax, Object.class);

        User inDb = userRepository.findByUsername("user1");
        assertThat(inDb.getHoaxes().size()).isEqualTo(1);
    }

    private <T> ResponseEntity<T> postHoax(Hoax hoax, Class<T> responseType) {
        return testRestTemplate.postForEntity(API_1_0_HOAXES, hoax, responseType);
    }

    private void authenticate(String username) {
        testRestTemplate.getRestTemplate().getInterceptors()
                .add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }
}
