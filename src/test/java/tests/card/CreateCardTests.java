package tests.card;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.card.CreateCardRequest;
import model.card.CreateCardResponse;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import tests.TestMaster;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static utils.ConstantUtils.*;
import static utils.ConstantUtils.POWER_BY;

public class CreateCardTests extends TestMaster {
    @Test
    void verifyCreateCardSuccessful(){

        WireMockServer refDataServer = new WireMockServer(options().port(7777)
//                .withRootDirectory("src/test/resources/mappings/ref")
                .notifier(new ConsoleNotifier(true))); //No-args constructor will start on port 8080, no HTTPS
        refDataServer.start();

        WireMockServer cardServer = new WireMockServer(options().port(7778)
//                .withRootDirectory("src/test/resources/mappings/card")
                .notifier(new ConsoleNotifier(true))); //No-args constructor will start on port 8080, no HTTPS
        cardServer.start();

        CreateCardRequest cardRequest = new CreateCardRequest("bf8b03eb-dcc8-4590-82d9-a5ad11f67798","SILVER");
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
        CreateCardResponse cardResponseActualExpected = new CreateCardResponse("Jose Doe","1111 2222 3333 4444","01-23-2028");
        softAssertions.assertThat(cardResponseActual.equals(cardResponseActualExpected)).isTrue();
        softAssertions.assertAll();
//        5. Using get api to check card was stored in system --> verify at System integration Test level
//        6. Access to DB to check card was stored in system --> verify at System integration Test level
    }
}
