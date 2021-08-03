package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.hoax.Hoax;
import com.hoaxify.hoaxify.hoax.HoaxRepository;
import com.hoaxify.hoaxify.hoax.HoaxService;
import com.hoaxify.hoaxify.hoax.HoaxVM;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import com.hoaxify.hoaxify.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;
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

    @Autowired
    HoaxService hoaxService;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

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
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();

        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsUnAuthorized_receiveUnauthorized() {
        Hoax hoax = TestUtil.createValidHoax();

        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsUnAuthorized_receiveApiError() {
        Hoax hoax = TestUtil.createValidHoax();

        ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);

        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxSavedToDB() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();

        postHoax(hoax, Object.class);

        assertThat(hoaxRepository.count()).isEqualTo(1);
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxSavedToDBWithTimestamp() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();

        postHoax(hoax, Object.class);

        Hoax inDb = hoaxRepository.findAll().get(0);

        assertThat(inDb.getTimestamp()).isNotNull();
    }

    @Test
    public void postHoax_whenHoaxContentIsNullAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();

        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postHoax_whenHoaxContentIsLessThan10CharsAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();
        hoax.setContent("123456789");

        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postHoax_whenHoaxContentIs5000CharsAndUserIsAuthorized_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();
        hoax.setContent(IntStream.rangeClosed(1, 5000).mapToObj(i -> "x").collect(Collectors.joining()));

        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postHoax_whenHoaxContentIsMoreThan5000CharsAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();
        hoax.setContent(IntStream.rangeClosed(1, 5001).mapToObj(i -> "x").collect(Collectors.joining()));

        ResponseEntity<Object> response = postHoax(hoax, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postHoax_whenHoaxContentIsNullAndUserIsAuthorized_receiveApiErrorWithValidationErrors() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();

        ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);

        assertThat(response.getBody().getValidationErrors().get("content")).isNotNull();
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxSavedWithAuthenticatedUserInfo() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();

        postHoax(hoax, Object.class);

        Hoax inDb = hoaxRepository.findAll().get(0);

        assertThat(inDb.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    // Open transaction to lazy load hoax entries
    @Transactional
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxCanBeAccessedFromUserEntity_transactional() {
        userService.save(TestUtil.createValidUser("user1"));
        TestTransaction.flagForCommit();
        TestTransaction.end();
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();

        postHoax(hoax, Object.class);

        TestTransaction.start();
        User inDb = userRepository.findByUsername("user1");
        assertThat(inDb.getHoaxes().size()).isEqualTo(1);
    }

    // Another way to bypass lazy load in test is to use the EntityManager
    @Test
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxCanBeAccessedFromUserEntity() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();

        postHoax(hoax, Object.class);

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        User inDb = entityManager.find(User.class, user.getId());
        assertThat(inDb.getHoaxes().size()).isEqualTo(1);
    }

    @Test
    public void getHoaxes_whenThereAreNoHoaxes_receiveOk() {
        ResponseEntity<Object> response = getHoaxes(new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getHoaxes_whenThereAreNoHoaxes_receivePageWithZeroItems() {
        ResponseEntity<TestPage<Object>> response = getHoaxes(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getHoaxes_whenThereAreHoaxes_receivePageWithItems() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);

        ResponseEntity<TestPage<Object>> response = getHoaxes(new ParameterizedTypeReference<TestPage<Object>>() {
        });

        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getHoaxes_whenThereAreHoaxes_receivePageWithHoaxVM() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(TestUtil.createValidHoax(), user);

        ResponseEntity<TestPage<HoaxVM>> response =
                getHoaxes(new ParameterizedTypeReference<TestPage<HoaxVM>>() {
                });
        HoaxVM storedHoax = response.getBody().getContent().get(0);

        assertThat(storedHoax.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_receiveHoaxVM() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();

        ResponseEntity<HoaxVM> response = postHoax(hoax, HoaxVM.class);

        assertThat(response.getBody().getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void getHoaxesOfUser_whenUserExists_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));

        ResponseEntity<Object> response = getHoaxesOfUser("user1", new ParameterizedTypeReference<Object>() {
        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getHoaxesOfUser_whenUserDoesNotExist_receiveNotFound() {
        // nothing given

        ResponseEntity<Object> response = getHoaxesOfUser("unknown-user", new ParameterizedTypeReference<Object>() {
        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getHoaxesOfUser_whenUserExists_receivePageWithZeroHoaxes() {
        userService.save(TestUtil.createValidUser("user1"));

        ResponseEntity<TestPage<Object>> response =
                getHoaxesOfUser("user1", new ParameterizedTypeReference<TestPage<Object>>() {
                });

        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getHoaxesOfUser_whenUserExistsWithHoaxe_receivePageWithHoaxVM() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(TestUtil.createValidHoax(), user);

        ResponseEntity<TestPage<HoaxVM>> response =
                getHoaxesOfUser("user1", new ParameterizedTypeReference<TestPage<HoaxVM>>() {
                });
        HoaxVM storedHoax = response.getBody().getContent().get(0);

        assertThat(storedHoax.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void getHoaxesOfUser_whenUserExistsWithHoaxes_receivePageWithMatchingHoaxesCount() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);

        ResponseEntity<TestPage<HoaxVM>> response =
                getHoaxesOfUser("user1", new ParameterizedTypeReference<TestPage<HoaxVM>>() {
                });

        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getHoaxesOfUser_whenMultipleUserExistsWithHoaxes_receivePageWithMatchingHoaxesCount() {
        User userWithThreeHoaxes = userService.save(TestUtil.createValidUser("user1"));
        IntStream.rangeClosed(1, 3).forEach(i -> {
            hoaxService.save(TestUtil.createValidHoax(), userWithThreeHoaxes);
        });

        User userWithFiveHoaxes = userService.save(TestUtil.createValidUser("user2"));
        IntStream.rangeClosed(1, 5).forEach(i -> {
            hoaxService.save(TestUtil.createValidHoax(), userWithFiveHoaxes);
        });

        ResponseEntity<TestPage<HoaxVM>> response =
                getHoaxesOfUser(userWithFiveHoaxes.getUsername(), new ParameterizedTypeReference<TestPage<HoaxVM>>() {
                });

        assertThat(response.getBody().getTotalElements()).isEqualTo(5);
    }

    @Test
    public void getOldHoaxes_whenThereAreNoHoaxes_receiveOk() {
        ResponseEntity<Object> response =
                getOldHoaxes(5, new ParameterizedTypeReference<Object>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getOldHoaxes_whenThereAreHoaxes_receivePageWithItemsProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        Hoax fourthHoax = hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);

        ResponseEntity<TestPage<Object>> response =
                getOldHoaxes(fourthHoax.getId(), new ParameterizedTypeReference<TestPage<Object>>() {
                });

        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getOldHoaxes_whenThereAreHoaxes_receivePageWithHoaxVMBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        Hoax fourthHoax = hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);

        ResponseEntity<TestPage<HoaxVM>> response =
                getOldHoaxes(fourthHoax.getId(), new ParameterizedTypeReference<TestPage<HoaxVM>>() {
                });

        assertThat(response.getBody().getContent().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getOldHoaxesOfUser_whenUserExistThereAreNoHoaxes_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));

        ResponseEntity<Object> response =
                getOldHoaxesOfUser(5,
                        "user1",
                        new ParameterizedTypeReference<Object>() {
                        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getOldHoaxesOfUser_whenUserExistAndThereAreHoaxes_receivePageWithItemsProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        Hoax fourthHoax = hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);

        ResponseEntity<TestPage<HoaxVM>> response =
                getOldHoaxesOfUser(fourthHoax.getId(),
                        "user1",
                        new ParameterizedTypeReference<TestPage<HoaxVM>>() {
                        });

        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getOldHoaxesOfUser_whenUserExistThereAreHoaxes_receivePageWithHoaxVMBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        Hoax fourthHoax = hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);

        ResponseEntity<TestPage<HoaxVM>> response =
                getOldHoaxesOfUser(fourthHoax.getId(),
                        "user1",
                        new ParameterizedTypeReference<TestPage<HoaxVM>>() {
                        });

        assertThat(response.getBody().getContent().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getOldHoaxesOfUser_whenUserDoesNotExistThereAreNoHoaxes_receiveNotFound() {

        ResponseEntity<Object> response =
                getOldHoaxesOfUser(5,
                        "user-does-not-exist",
                        new ParameterizedTypeReference<Object>() {
                        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getOldHoaxesOfUser_whenUserExistThereAreNoHoaxes_receivePageWithZeroItemsBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        Hoax fourthHoax = hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);

        userService.save(TestUtil.createValidUser("user2"));


        ResponseEntity<TestPage<HoaxVM>> response =
                getOldHoaxesOfUser(fourthHoax.getId(),
                        "user2",
                        new ParameterizedTypeReference<TestPage<HoaxVM>>() {
                        });

        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getNewHoaxes_whenThereAreHoaxes_receiveListOfItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        Hoax fourthHoax = hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);

        ResponseEntity<List<Object>> response =
                getNewHoaxes(fourthHoax.getId(), new ParameterizedTypeReference<List<Object>>() {
                });

        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void getNewHoaxes_whenThereAreHoaxes_receiveListOfHoaxVMAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        Hoax fourthHoax = hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);

        ResponseEntity<List<HoaxVM>> response =
                getNewHoaxes(fourthHoax.getId(), new ParameterizedTypeReference<List<HoaxVM>>() {
                });

        assertThat(response.getBody().get(0).getDate()).isGreaterThan(0);
    }












    @Test
    public void getNewHoaxes_whenThereAreNoHoaxes_receiveOk() {
        ResponseEntity<Object> response =
                getNewHoaxes(5, new ParameterizedTypeReference<Object>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getNewHoaxes_whenThereAreHoaxes_receiveListWithItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        Hoax fourthHoax = hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);

        ResponseEntity<List<Object>> response =
                getNewHoaxesOfUser(fourthHoax.getId(), "user1", new ParameterizedTypeReference<List<Object>>() {
                });

        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void getNewHoaxes_whenThereAreHoaxes_receiveListWithHoaxVMAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        Hoax fourthHoax = hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);

        ResponseEntity<List<HoaxVM>> response =
                getNewHoaxesOfUser(fourthHoax.getId(), "user1", new ParameterizedTypeReference<List<HoaxVM>>() {
                });

        assertThat(response.getBody().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getNewHoaxesOfUser_whenUserDoesNotExistThereAreNoHoaxes_receiveNotFound() {

        ResponseEntity<Object> response =
                getNewHoaxesOfUser(5,
                        "user-does-not-exist",
                        new ParameterizedTypeReference<Object>() {
                        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getNewHoaxesOfUser_whenUserExistThereAreNoHoaxes_receivePageWithZeroItemsBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);
        Hoax fourthHoax = hoaxService.save(TestUtil.createValidHoax(), user);
        hoaxService.save(TestUtil.createValidHoax(), user);

        userService.save(TestUtil.createValidUser("user2"));


        ResponseEntity<List<HoaxVM>> response =
                getNewHoaxesOfUser(fourthHoax.getId(),
                        "user2",
                        new ParameterizedTypeReference<List<HoaxVM>>() {
                        });

        assertThat(response.getBody().size()).isEqualTo(0);
    }

    /*
    Private Test Helper Methods
     */

    private <T> ResponseEntity<T> getNewHoaxes(long hoaxId, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_HOAXES + "/" + hoaxId + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> getOldHoaxes(long hoaxId, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_HOAXES + "/" + hoaxId + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> getNewHoaxesOfUser(long hoaxId,
                                                     String username,
                                                     ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/"
                + username
                + "/hoaxes/"
                + hoaxId
                + "?direction=after&sort=id,desc";

        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> getOldHoaxesOfUser(long hoaxId,
                                                     String username,
                                                     ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/"
                + username
                + "/hoaxes/"
                + hoaxId
                + "?direction=before&page=0&size=5&sort=id,desc";

        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> getHoaxesOfUser(String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/hoaxes";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> getHoaxes(ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(API_1_0_HOAXES, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> postHoax(Hoax hoax, Class<T> responseType) {
        return testRestTemplate.postForEntity(API_1_0_HOAXES, hoax, responseType);
    }

    private void authenticate(String username) {
        testRestTemplate.getRestTemplate().getInterceptors()
                .add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }
}
