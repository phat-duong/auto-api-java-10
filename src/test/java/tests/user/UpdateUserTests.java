package tests.user;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.user.dao.CustomerAddressDao;
import model.user.dao.CustomerDao;
import model.user.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.DbUtils;
import utils.LoginUtils;
import utils.RestAssuredUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateUserTests {

    static String token;
    static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    static final String HEADER_AUTHORIZATION = "Authorization";
    static final String HEADER_CONTENT_TYPE = "Content-Type";
    static final String CONTENT_TYPE = "application/json; charset=utf-8";
    static final String HEADER_POWER_BY = "X-Powered-By";
    static final String POWER_BY = "Express";
    static final String EMAIL_TEMPLATE = "auto_api_%s@abc.com";
    static final String CREATE_USER_API = "/api/user";
    static final String GET_USER_API = "/api/user/%s";
    static final String DELETE_USER_API = "/api/user/%s";
    static final String UPDATE_USER_API = "/api/user/%s";
    static List<String> createdCustomerIds = new ArrayList<>();

    @BeforeAll
    static void setUp(){
        RestAssuredUtils.setUp();
    }

    @BeforeEach
    void beforeEach(){
        token = LoginUtils.getToken();
    }

    @AfterAll
    static void tearDown(){
        //6. Clean up data
        for(String id: createdCustomerIds){
            RestAssured.given().log().all()
                    .header(HEADER_AUTHORIZATION, token)
                    .delete(String.format(DELETE_USER_API, id));
        }
    }
    @Test
    void verifyUpdateUserSuccessfull(){

        // 1. Prepare data
        long randomNumber = System.currentTimeMillis();
        String randomEmail = String.format(EMAIL_TEMPLATE, randomNumber);

        UserRequest userRequest = UserRequest.getDefault();
        userRequest.setEmail(randomEmail);

        LocalDateTime timeBeforeCreateUser = LocalDateTime.now(ZoneId.of("Z"));
        LocalDateTime timeBeforeCreateUserForDB = LocalDateTime.now();

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HEADER_AUTHORIZATION, token)
                .body(userRequest)
                .post(CREATE_USER_API);

        assertThat(response.statusCode()).isEqualTo(200);
        CreateUserResponse createUserResponse = response.as(CreateUserResponse.class);
        createdCustomerIds.add(createUserResponse.getId());

        //2. Perform updating
        UserRequest updateUserRequest = UserRequest.getUpdateUserInfo();
        updateUserRequest.setEmail(randomEmail);
        Response updateResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HEADER_AUTHORIZATION, token)
                .body(updateUserRequest)
                .put(String.format(UPDATE_USER_API, createUserResponse.getId()));

        SoftAssertions softAssertions = new SoftAssertions();

        //        3. Status code
        softAssertions.assertThat(updateResponse.statusCode()).isEqualTo(200);
//        4. Verify header if needs
        softAssertions.assertThat(updateResponse.header(HEADER_CONTENT_TYPE)).isEqualTo(CONTENT_TYPE);
        softAssertions.assertThat(updateResponse.header(HEADER_POWER_BY)).isEqualTo(POWER_BY);
//        5. Verify body
//        5.1. verify schema -> do it in a separate testcase
//        5.2. Verify response
        UpdateUserResponse updateUserResponse = updateResponse.as(UpdateUserResponse.class);
        softAssertions.assertThat(updateUserResponse.getId()).isEqualTo(createUserResponse.getId());
        softAssertions.assertThat(updateUserResponse.getMessage()).isEqualTo("Customer updated");

//        6. Double check that user has been stored in system
        Response getResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HEADER_AUTHORIZATION, token)
                .get(String.format(GET_USER_API, createUserResponse.getId()));
        softAssertions.assertThat(getResponse.statusCode()).isEqualTo(200);
        softAssertions.assertAll();
        GetUserResponse getUserResponse = getResponse.as(GetUserResponse.class);

        assertThatJson(getUserResponse)
                .whenIgnoringPaths("$..id", "$..createdAt", "$..updatedAt", "$..customerId")
                .isEqualTo(updateUserRequest);
        softAssertions = new SoftAssertions();
        softAssertions.assertThat(getUserResponse.getId()).isEqualTo(createUserResponse.getId());

        LocalDateTime timeAfterCreateUser = LocalDateTime.now(ZoneId.of("Z"));
        LocalDateTime timeAfterCreateUserForDB = LocalDateTime.now();


        for (GetUserAddressResponse address: getUserResponse.getAddresses()) {
            softAssertions.assertThat(address.getCustomerId()).isEqualTo(createUserResponse.getId());

            verifyDateTime(softAssertions, address.getCreatedAt(), timeBeforeCreateUser, timeAfterCreateUser);

            verifyDateTime(softAssertions, address.getUpdatedAt(), timeBeforeCreateUser, timeAfterCreateUser);

            softAssertions.assertAll();
        }

        verifyDateTime(softAssertions, getUserResponse.getCreatedAt(), timeBeforeCreateUser, timeAfterCreateUser);
        verifyDateTime(softAssertions, getUserResponse.getUpdatedAt(), timeBeforeCreateUser, timeAfterCreateUser);
        softAssertions.assertAll();

        // 7. Verify by access to DB
        CustomerDao customerDao = DbUtils.getCustomerFromDB(createUserResponse.getId());
        assertThatJson(customerDao)
                .whenIgnoringPaths("$..id", "$..createdAt", "$..updatedAt", "$..customerId")
                .isEqualTo(updateUserRequest);
        softAssertions = new SoftAssertions();
        softAssertions.assertThat(UUID.fromString(getUserResponse.getId())).isEqualTo(customerDao.getId());

        for (CustomerAddressDao address: customerDao.getAddresses()) {
            softAssertions.assertThat(address.getCustomerId()).isEqualTo(UUID.fromString(createUserResponse.getId()));

            verifyDateTimeDb(softAssertions, address.getCreatedAt(), timeBeforeCreateUserForDB, timeAfterCreateUserForDB);
            verifyDateTimeDb(softAssertions, address.getUpdatedAt(), timeBeforeCreateUserForDB, timeAfterCreateUserForDB);
        }
        verifyDateTimeDb(softAssertions, customerDao.getCreatedAt(), timeBeforeCreateUserForDB, timeAfterCreateUserForDB);
        verifyDateTimeDb(softAssertions, customerDao.getUpdatedAt(), timeBeforeCreateUserForDB, timeAfterCreateUserForDB);
        softAssertions.assertAll();
    }

    void verifyDateTime(SoftAssertions softAssertions, String targetDateTime, LocalDateTime timeBefore, LocalDateTime timeAfter){
        LocalDateTime userUpdatedAt = LocalDateTime.parse(targetDateTime, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        softAssertions.assertThat(userUpdatedAt.isAfter(timeBefore)).isTrue();
        softAssertions.assertThat(userUpdatedAt.isBefore(timeAfter)).isTrue();
    }

    void verifyDateTimeDb(SoftAssertions softAssertions, LocalDateTime targetDateTime, LocalDateTime timeBefore, LocalDateTime timeAfter){
        softAssertions.assertThat(targetDateTime.isAfter(timeBefore)).isTrue();
        softAssertions.assertThat(targetDateTime.isBefore(timeAfter)).isTrue();
    }
}

