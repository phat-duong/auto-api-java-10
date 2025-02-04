package utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.login.LoginRequest;
import model.login.LoginResponse;

import java.time.LocalDateTime;

//import static org.hamcrest.MatcherAssert.assertThat;


public class LoginUtils {
//    public static String getToken(){
//        LoginRequest loginRequest = LoginRequest.getDefault();
//        Response response = RestAssured.given().log().all()
//                .contentType(ContentType.JSON)
//                .body(loginRequest)
//                .post("/api/login");
////        assertThat(response.statusCode()).isEqualTo(200);
//        LoginResponse loginResponse = response.as(LoginResponse.class);
//        return String.format("Bearer %s", loginResponse.getToken());
//    }

//    xử lý token expire
    private static LocalDateTime tokenCreated;
    private static LoginResponse loginResponse;

    public static String getToken(){
        if(tokenCreated == null){
            return login();
        }else {
            long timeout = (long) (Long.parseLong(loginResponse.getTimeout())*0.8);
            if (LocalDateTime.now().isAfter(tokenCreated.plusSeconds(timeout))){
                return login();
            }else {
                return String.format("Bearer %s", loginResponse.getToken());
            }
        }
    }
    private static String login(){
        LoginRequest loginRequest = LoginRequest.getDefault();
        tokenCreated = LocalDateTime.now();
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/api/login");
//        assertThat(response.statusCode()).isEqualTo(200);
        loginResponse = response.as(LoginResponse.class);
        return String.format("Bearer %s", loginResponse.getToken());
    }
}
