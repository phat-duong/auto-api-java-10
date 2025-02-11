package tests.graphql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.RestAssuredUtils;

import java.util.HashMap;
import java.util.Map;

import static data.graphql.CountriesQuery.EXPECTED_COUNTRY_QUERY_DATA;
import static data.graphql.CountriesQuery.GET_COUNTRIES_QUERY;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static utils.ConstantUtils.CREATE_USER_API;
import static utils.ConstantUtils.HEADER_AUTHORIZATION;

public class GraphQLTests {
    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "https://countries.trevorblades.com/";
//        RestAssured.port=443;
    }

    @Test
    void verifyQueryCountriesSuccessful(){
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", GET_COUNTRIES_QUERY);

        Map<String, String> variables = new HashMap<>();
        variables.put("vnCode", "VN");
        variables.put("brCode", "BR");
        requestBody.put("variables", variables);

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post();
        SoftAssertions softAssertions = new SoftAssertions();
        System.out.println(response.asString());
//        1. Verify status code
        softAssertions.assertThat(response.statusCode()).isEqualTo(200);

        //        2. Verify header
        softAssertions.assertThat(response.header("stellate-rate-limit-decision")).isEqualTo("pass");
//        3. Verify body schema
//        4. Verify body values

        softAssertions.assertAll();
        assertThatJson(response.asString()).isEqualTo(EXPECTED_COUNTRY_QUERY_DATA);
    }
}
