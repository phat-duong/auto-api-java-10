package tests.user;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.user.dao.CustomerAddressDao;
import model.user.dao.CustomerDao;
import model.user.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CreateUserTests {

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
    void verifyCreateUserSuccessful(){
//        String body = """
//                {
//                    "firstName":"Jos",
//                    "lastName": "Doe",
//                    "middleName": "Smith",
//                    "birthday": "01-23-2000",
//                    "email": "auto_api_1sa19922121@abc.com",
//                    "phone": "0123456789",
//                    "addresses": [
//                        {
//                            "streetNumber": "123",
//                            "street": "Main St",
//                            "ward": "Ward 1",
//                            "district": "District 1",
//                            "city": "Thu Duc",
//                            "state": "Ho Chi Minh",
//                            "zip": "70000",
//                            "country": "VN"
//                        }
//                    ]
//                }
//                """;

//        LoginRequest loginRequest = LoginRequest.getDefault();
//        LoginResponse loginResponse = RestAssured.given().log().all()
//                .contentType(ContentType.JSON)
//                .body(loginRequest)
//                .post("/api/login")
//                .as(LoginResponse.class);
//        String token = String.format("Bearer %s", loginResponse.getToken());
        long randomNumber = System.currentTimeMillis();
        String randomEmail = String.format(EMAIL_TEMPLATE, randomNumber);


//        UserAddressRequest userAddressRequest = new UserAddressRequest("123", "Main St", "Ward 1", "District 1", "Thu Duc", "Ho Chi Minh", "70000","VN");
        UserAddressRequest userAddressRequest = UserAddressRequest.getDefault();
//        UserRequest userRequest = new UserRequest("Jos", "Doe", "Smith", "01-23-2000", randomEmail, "0123456789", List.of(userAddressRequest));
        UserRequest userRequest = UserRequest.getDefault();
        userRequest.setEmail(randomEmail);

        LocalDateTime timeBeforeCreateUser = LocalDateTime.now(ZoneId.of("Z"));
        LocalDateTime timeBeforeCreateUserForDB = LocalDateTime.now();
//        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InN0YWZmIiwiaWF0IjoxNzM2NzczNTI2LCJleHAiOjE3MzY4OTM1MjZ9.cy5hdG6v8mDQheKNS5iwodEH79Uoj1VZUHnK0r-BC30";
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HEADER_AUTHORIZATION, token)
                .body(userRequest)
                .post(CREATE_USER_API);

        SoftAssertions softAssertions = new SoftAssertions();
//        1. Status code
        softAssertions.assertThat(response.statusCode()).isEqualTo(200);
//        2. Verify header if needs
        softAssertions.assertThat(response.header(HEADER_CONTENT_TYPE)).isEqualTo(CONTENT_TYPE);
        softAssertions.assertThat(response.header(HEADER_POWER_BY)).isEqualTo(POWER_BY);
//        3. Verify body
//        3.1. verify schema -> do it in a separate testcase
//        3.2. Verify response
        CreateUserResponse createUserResponse = response.as(CreateUserResponse.class);
        createdCustomerIds.add(createUserResponse.getId());
        softAssertions.assertThat(StringUtils.isNoneBlank(createUserResponse.getId())).isTrue();
        softAssertions.assertThat(createUserResponse.getMessage()).isEqualTo("Customer created");

//        4. Double check that user has been stored in system
        Response getResponse = RestAssured.given().log().all()
                        .contentType(ContentType.JSON)
                        .header(HEADER_AUTHORIZATION, token)
                        .get(String.format(GET_USER_API, createUserResponse.getId()));
        softAssertions.assertThat(getResponse.statusCode()).isEqualTo(200);
        softAssertions.assertAll();
        GetUserResponse getUserResponse = getResponse.as(GetUserResponse.class);

        assertThatJson(getUserResponse)
                .whenIgnoringPaths("$..id", "$..createdAt", "$..updatedAt", "$..customerId")
                .isEqualTo(userRequest);
        softAssertions = new SoftAssertions();
        softAssertions.assertThat(getUserResponse.getId()).isEqualTo(createUserResponse.getId());

        LocalDateTime timeAfterCreateUser = LocalDateTime.now(ZoneId.of("Z"));
        LocalDateTime timeAfterCreateUserForDB = LocalDateTime.now();


        for (GetUserAddressResponse address: getUserResponse.getAddresses()) {
            softAssertions.assertThat(address.getCustomerId()).isEqualTo(createUserResponse.getId());

//            LocalDateTime userAddressCreatedAt = LocalDateTime.parse(getUserResponse.getCreatedAt(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
//            softAssertions.assertThat(userAddressCreatedAt.isAfter(timeBeforeCreateUser)).isTrue();
//            softAssertions.assertThat(userAddressCreatedAt.isBefore(timeAfterCreateUser)).isTrue();
            verifyDateTime(softAssertions, address.getCreatedAt(), timeBeforeCreateUser, timeAfterCreateUser);

//            LocalDateTime userAddressUpdatedAt = LocalDateTime.parse(getUserResponse.getUpdatedAt(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
//            softAssertions.assertThat(userAddressUpdatedAt.isAfter(timeBeforeCreateUser)).isTrue();
//            softAssertions.assertThat(userAddressUpdatedAt.isBefore(timeAfterCreateUser)).isTrue();
            verifyDateTime(softAssertions, address.getUpdatedAt(), timeBeforeCreateUser, timeAfterCreateUser);

            softAssertions.assertAll();
        }

//        LocalDateTime userCreatedAt = LocalDateTime.parse(getUserResponse.getCreatedAt(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
//        softAssertions.assertThat(userCreatedAt.isAfter(timeBeforeCreateUser)).isTrue();
//        softAssertions.assertThat(userCreatedAt.isBefore(timeAfterCreateUser)).isTrue();
        verifyDateTime(softAssertions, getUserResponse.getCreatedAt(), timeBeforeCreateUser, timeAfterCreateUser);

//        LocalDateTime userUpdatedAt = LocalDateTime.parse(getUserResponse.getUpdatedAt(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
//        softAssertions.assertThat(userUpdatedAt.isAfter(timeBeforeCreateUser)).isTrue();
//        softAssertions.assertThat(userUpdatedAt.isBefore(timeAfterCreateUser)).isTrue();
        verifyDateTime(softAssertions, getUserResponse.getUpdatedAt(), timeBeforeCreateUser, timeAfterCreateUser);

        softAssertions.assertAll();

        // 5. Verify by access to DB
        CustomerDao customerDao = DbUtils.getCustomerFromDB(createUserResponse.getId());
        assertThatJson(customerDao)
                .whenIgnoringPaths("$..id", "$..createdAt", "$..updatedAt", "$..customerId")
                .isEqualTo(userRequest);
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

    @Test
    void verifyCreateUserSuccessfulWithTwoAddress(){
//        LoginRequest loginRequest = LoginRequest.getDefault();
//        LoginResponse loginResponse = RestAssured.given().log().all()
//                .contentType(ContentType.JSON)
//                .body(loginRequest)
//                .post("/api/login")
//                .as(LoginResponse.class);
//        String token = String.format("Bearer %s", loginResponse.getToken());
        long randomNumber = System.currentTimeMillis();
        String randomEmail = String.format("auto_api_%s@abc.com", randomNumber);
//        UserAddressRequest userAddressRequest = new UserAddressRequest("123", "Main St", "Ward 1", "District 1", "Thu Duc", "Ho Chi Minh", "70000","VN");
        UserAddressRequest userAddressRequest1 = UserAddressRequest.getDefault();
        UserAddressRequest userAddressRequest2 = UserAddressRequest.getDefault();
        userAddressRequest2.setStreetNumber("456");
//        UserRequest userRequest = new UserRequest("Jos", "Doe", "Smith", "01-23-2000", randomEmail, "0123456789", List.of(userAddressRequest));
        UserRequest userRequest = UserRequest.getDefault();
        userRequest.setEmail(randomEmail);
        userRequest.setAddresses(List.of(userAddressRequest1, userAddressRequest2));
//        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InN0YWZmIiwiaWF0IjoxNzM2NzczNTI2LCJleHAiOjE3MzY4OTM1MjZ9.cy5hdG6v8mDQheKNS5iwodEH79Uoj1VZUHnK0r-BC30";
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(userRequest)
                .post("/api/user");

        SoftAssertions softAssertions = new SoftAssertions();
//        1. Status code
        softAssertions.assertThat(response.statusCode()).isEqualTo(200);
//        2. Verify header if needs
        softAssertions.assertThat(response.header("Content-Type")).isEqualTo("application/json; charset=utf-8");
        softAssertions.assertThat(response.header("X-Powered-By")).isEqualTo("Express");
//        3. Verify body
//        3.1. verify schema -> do it in a separate testcase
//        3.2. Verify response
        CreateUserResponse createUserResponse = response.as(CreateUserResponse.class);
        softAssertions.assertThat(StringUtils.isNoneBlank(createUserResponse.getId())).isTrue();
        softAssertions.assertThat(createUserResponse.getMessage()).isEqualTo("Customer created");
        softAssertions.assertAll();
    }
}
