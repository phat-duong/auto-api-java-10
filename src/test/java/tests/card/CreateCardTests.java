package tests.card;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.card.CreateCardRequest;
import model.card.CreateCardResponse;
import model.user.dto.CreateUserResponse;
import model.user.dto.UserAddressRequest;
import model.user.dto.UserRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tests.TestMaster;
import utils.StubUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static utils.ConstantUtils.*;
import static utils.ConstantUtils.POWER_BY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CreateCardTests extends TestMaster {

    @BeforeAll
    static void setUpForCard(){
        StubUtils.startStubForCreateCard();
    }
    @Test
    void verifyCreateCardSuccessful(){

        long randomNumber = System.currentTimeMillis();
        String randomEmail = String.format(EMAIL_TEMPLATE, randomNumber);
        UserRequest userRequest = UserRequest.getDefault();
        userRequest.setFirstName("Jos");
        userRequest.setLastName("Doe");
        userRequest.setEmail(randomEmail);

        Response createUserResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HEADER_AUTHORIZATION, token)
                .body(userRequest)
                .post(CREATE_USER_API);
        assertThat(createUserResponse.statusCode()).isEqualTo(200);
        CreateUserResponse createUserResponseBody = createUserResponse.as(CreateUserResponse.class);

//        Verify create card
        CreateCardRequest cardRequest = new CreateCardRequest(createUserResponseBody.getId(),"SILVER");
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HEADER_AUTHORIZATION, token)
                .body(cardRequest)
                .post(CREATE_CARD_API);
        SoftAssertions softAssertions = new SoftAssertions();
//        1. Verify status code
        softAssertions.assertThat(response.statusCode()).isEqualTo(200);
//        2. Verify headers
        softAssertions.assertThat(response.header(HEADER_CONTENT_TYPE)).isEqualTo(CONTENT_TYPE);
        softAssertions.assertThat(response.header(HEADER_POWER_BY)).isEqualTo(POWER_BY);
//        3. Verify body schema
//        --> implement in a separate testcase

//        4. Verify body value
        CreateCardResponse cardResponseActual = response.as(CreateCardResponse.class);
        CreateCardResponse cardResponseActualExpected = new CreateCardResponse(String.format("%s %s", userRequest.getLastName(), userRequest.getFirstName()),"1111 2222 3333 4444","01-23-2028");
        softAssertions.assertThat(cardResponseActual.equals(cardResponseActualExpected)).isTrue();
        softAssertions.assertAll();
//        5. Using get api to check card was stored in system --> verify at System integration Test level
//        6. Access to DB to check card was stored in system --> verify at System integration Test level
    }
}
