package tests.country;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import model.country.CountryPagination;

import org.hamcrest.Matcher;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static data.country.GetCountryData.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GetCountryTests {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    void testCountriesSchema() {
        RestAssured.given().log().all()
                .get("/api/v1/countries")
                .then().log().all()
                .statusCode(200)
                .assertThat().body(matchesJsonSchemaInClasspath("data/get-country/get-country-json-schema.json"));
    }

    @Test
    void verifyGetCountriesApiResponseCorrectData() {
        Response response = RestAssured.given().log().all()
                .get("/api/v1/countries");
//        1. Status code
        assertThat(response.statusCode(), equalTo(200));
//        2. Verify header if needs
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
//        3. Verify body
        assertThat(response.asString(), jsonEquals(GET_ALL_COUNTRIES).when(IGNORING_ARRAY_ORDER));
        System.out.println(response.asString());
    }

    @Test
    void testCountriesWithGDPSchema() {
        RestAssured.given().log().all()
                .get("/api/v2/countries")
                .then().log().all()
                .statusCode(200)
                .assertThat().body(matchesJsonSchemaInClasspath("data/get-country/get-country-with-gdp-json-schema.json"));
    }

    @Test
    void verifyGetCountriesWithGDPApiResponseCorrectData() {
        Response response = RestAssured.given().log().all()
                .get("/api/v2/countries");
//        1. Status code
        assertThat(response.statusCode(), equalTo(200));
//        2. Verify header if needs
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
//        3. Verify body
        assertThat(response.asString(), jsonEquals(GET_ALL_COUNTRIES_WITH_GDP).when(IGNORING_ARRAY_ORDER));
        System.out.println(response.asString());
    }


    @Test
    void verifyGetCountryApiResponseCorrectData() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> data = mapper.readValue(GET_ALL_COUNTRIES, new TypeReference<List<Map<String, String>>>() {
        });
        for (Map<String, String> country : data) {
            Response response = RestAssured.given().log().all()
                    .get("/api/v1/countries/{code}", country.get("code"));
//        1. Status code
            assertThat(response.statusCode(), equalTo(200));
//        2. Verify header if needs
            assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
            assertThat(response.header("X-Powered-By"), equalTo("Express"));
//        3. Verify body
            assertThat(response.asString(), jsonEquals(country));
            System.out.println(response.asString());
        }
    }


    static Stream<?> verifyGetCountriesWithFilter(){
        List<Map<String, String>> inputs = new ArrayList<>();
        inputs.add(Map.of("gdp", "1868", "operator", ">"));
        inputs.add(Map.of("gdp", "1868", "operator", "<"));
        inputs.add(Map.of("gdp", "1868", "operator", ">="));
        inputs.add(Map.of("gdp", "1868", "operator", "<="));
        inputs.add(Map.of("gdp", "1868", "operator", "=="));
        inputs.add(Map.of("gdp", "1868", "operator", "!="));
        return inputs.stream();
    }

    @ParameterizedTest
    @MethodSource("verifyGetCountriesWithFilter")
    void verifyGetCountriesWithFilterGreaterThan(Map<String, String> queryParams){
        Response response = RestAssured.given().log().all()
                .queryParams(queryParams)
                .get("/api/v3/countries");
//        1. Status code
        assertThat(response.statusCode(), equalTo(200));
//        2. Verify header if needs
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
//        3. Verify body
        List<Map<String, String>> countries = response.as(new TypeRef<List<Map<String, String>>>() {
        });
        for(Map<String, String> country : countries){
            float actualGDP = Float.parseFloat(queryParams.get("gdp"));
            Matcher<Float> matcher = switch (queryParams.get("operator")){
                case ">" -> greaterThan(actualGDP);
                case "<" -> lessThan(actualGDP);
                case ">=" -> greaterThanOrEqualTo(actualGDP);
                case "<=" -> lessThanOrEqualTo(actualGDP);
                case "!=" -> not(equalTo(actualGDP));
                default -> equalTo(actualGDP);
            };
            assertThat(Float.parseFloat(country.get("gdp")), matcher);

//            if(">".equals(queryParams.get("operator"))){
//                assertThat(Float.parseFloat(country.get("gdp")), greaterThan(Float.parseFloat(queryParams.get("gdp"))));
//            } else if ("<".equals(queryParams.get("operator"))) {
//                assertThat(Float.parseFloat(country.get("gdp")), lessThan(Float.parseFloat(queryParams.get("gdp"))));
//            }
//            else if (">=".equals(queryParams.get("operator"))) {
//                assertThat(Float.parseFloat(country.get("gdp")), greaterThanOrEqualTo(Float.parseFloat(queryParams.get("gdp"))));
//            }else if ("<=".equals(queryParams.get("operator"))) {
//                assertThat(Float.parseFloat(country.get("gdp")), lessThanOrEqualTo(Float.parseFloat(queryParams.get("gdp"))));
//            }else if ("==".equals(queryParams.get("operator"))) {
//                assertThat(Float.parseFloat(country.get("gdp")), equalTo(Float.parseFloat(queryParams.get("gdp"))));
//            }else if ("!=".equals(queryParams.get("operator"))) {
//                assertThat(Float.parseFloat(country.get("gdp")), not(equalTo(Float.parseFloat(queryParams.get("gdp")))));
//            }
        }
    }

    @Test
    void verifygetCountryApiWithPagination(){
        int page = 1;
        int size = 4;
//        Verify first page
        Response response = getCountries(page, size);
        CountryPagination countryFirstPage = response.as(CountryPagination.class);

        verifyCountriesResponse(size, response, countryFirstPage);


//        Verify second page
        response = getCountries(page+1, size);

        CountryPagination countrySecondPage = response.as(CountryPagination.class);

        verifyCountriesResponse(size, response, countrySecondPage);

        //Verify data of first page difference from second page
        assertThat(countrySecondPage.getData().containsAll(countryFirstPage.getData()), is(false));

        //Verify last page
        int total = countryFirstPage.getTotal();
        double lastPage = Math.ceil(total/size);
        double sizeOfLastPage = total - lastPage * size;
        if (sizeOfLastPage == 0){
            sizeOfLastPage = size;
        }
        response = getCountries(lastPage, size);

        CountryPagination countryLastPage = response.as(CountryPagination.class);

        verifyCountriesResponse(size, response, countryLastPage);

    }

    private static void verifyCountriesResponse(int size, Response response, CountryPagination countryFirstPage) {
        //        1. Status code
        assertThat(response.statusCode(), equalTo(200));
//        2. Verify header if needs
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
//        3. Verify body
        assertThat(countryFirstPage.getData().size(), equalTo(size));
    }

    private static Response getCountries(double page, int size) {
        Response response = RestAssured.given().log().all()
                .queryParam("page", page)
                .queryParam("size", size)
                .get("/api/v4/countries");
        return response;
    }

    @Test
    void verifyGetCountriesWithPrivateKeyResponseCorrectData() {
        Response response = RestAssured.given().log().all()
                .header("api-key", "private")
                .get("/api/v5/countries");
//        1. Status code
        assertThat(response.statusCode(), equalTo(200));
//        2. Verify header if needs
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
//        3. Verify body
//        3.1. verify schema -> do it in a separate testcase
//        3.2. Verify response
        assertThat(response.asString(), jsonEquals(GET_ALL_COUNTRIES_PRIVATE).when(IGNORING_ARRAY_ORDER));
    }
}