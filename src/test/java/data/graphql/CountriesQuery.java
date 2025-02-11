package data.graphql;

public class CountriesQuery {
    public static final String EXPECTED_COUNTRY_QUERY_DATA = """
                {
                    "data": {
                        "vn": {
                            "name": "Vietnam",
                            "currency": "VND",
                            "languages": [
                                {
                                    "code": "vi",
                                    "name": "Vietnamese"
                                }
                            ]
                        },
                        "br": {
                            "name": "Brazil",
                            "currency": "BRL"
                        }
                    }
                }
                """;

    public static final String GET_COUNTRIES_QUERY = """
            query getCountries($vnCode: ID!, $brCode: ID!) {
              vn: country(code: $vnCode) {
                name
                currency
                languages {
                  code
                  name
                }
              }
              br: country(code: $brCode) {
                name
                currency
              }
            }
            """;
}
