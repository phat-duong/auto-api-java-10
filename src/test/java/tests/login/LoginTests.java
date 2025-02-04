package tests.login;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.login.LoginFailResponse;
import model.login.LoginRequest;
import model.login.LoginResponse;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import utils.RestAssuredUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class LoginTests {

    @BeforeAll
    static void setUp() {
        RestAssuredUtils.setUp();
    }
    @Test
    void verifyUserLoginSuccesful(){
        LoginRequest loginRequest = new LoginRequest("staff", "1234567890");
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/api/login");

//        1. Status code
        assertThat(response.statusCode(), equalTo(200));
//        2. Verify header if needs
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
//        3. Verify body
//        3.1. verify schema -> do it in a separate testcase
//        3.2. Verify response
        LoginResponse loginResponse = response.as(LoginResponse.class);
        assertThat(StringUtils.isNoneBlank(loginResponse.getToken()), is(true));
        assertThat(loginResponse.getTimeout(), equalTo("120000"));
    }

    @ParameterizedTest
    @CsvSource({
            "staffs, 1234567890",
            "staff, 12345678901",
            "'', 1234567890",
            ", 1234567890",
            "staff, ''",
            "staff, "

    })
    void verifyUserLoginFail(String username, String password){
        LoginRequest loginRequest = new LoginRequest(username, password);
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/api/login");

        SoftAssertions softAssertions = new SoftAssertions();
//        1. Status code
        softAssertions.assertThat(response.statusCode()).isEqualTo(401);
//        assertThat(response.statusCode(), equalTo(401));
//        2. Verify header if needs
        softAssertions.assertThat(response.header("Content-Type")).isEqualTo("application/json; charset=utf-8");
        softAssertions.assertThat(response.header("X-Powered-By")).isEqualTo("Express");

//        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
//        assertThat(response.header("X-Powered-By"), equalTo("Express"));
//        3. Verify body
//        3.1. verify schema -> do it in a separate testcase
//        3.2. Verify response
        LoginFailResponse loginFailResponse = response.as(LoginFailResponse.class);
//        assertThat(loginFailResponse.getMessage(), equalTo("Invalid credentials"));
        softAssertions.assertThat(loginFailResponse.getMessage()).isEqualTo("Invalid credentials");
        softAssertions.assertAll();


    }
}
