
import consts.ApiMethods;
import consts.Constant;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static consts.Constant.*;
import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.*;

public class CurrencyAPITest {


    @ParameterizedTest
    @ValueSource(strings = {CURRENCY_PAIR1, CURRENCY_PAIR2, CURRENCY_PAIR3, CURRENCY_PAIR4, CURRENCY_PAIR5, CURRENCY_PAIR6, CURRENCY_PAIR7, CURRENCY_PAIR8})
    public void SanityDTestWithCorrectKey(String value) {
        Response response = ApiMethods.SanityWithCorrectKey();
        response.then().statusCode(200);
        response.then().body("success", equalTo(true))
                .body(containsString(value));
    }

    @Test
    public void noAccessKeyTest() {
        Response response = ApiMethods.noAccessKey();
        response.
                then().statusCode(200).
                and().body("success", equalTo(false)).
                and().body("error.code", equalTo(101)).
                and().body("error.type", equalTo("missing_access_key"));
    }


    @ParameterizedTest
    @ValueSource(strings = {VALUE1, VALUE2, VALUE3, VALUE4, VALUE5})
    public void getCurrencyTest(String value) {
       Response response = ApiMethods.getCurrency(value);
        response.then().statusCode(200);
        response.then().body("success", equalTo(true))
                .body("terms", notNullValue())
                .body("privacy", notNullValue())
                .body("timestamp", notNullValue())
                .body("source", equalTo(DEFAULT_CURRENCY))
                .body(String.format("quotes.%s%s", DEFAULT_CURRENCY, value), notNullValue());
    }


    @ParameterizedTest
    @DisplayName("The current Subscription Plan does not support Source Currency Switching")
    @ValueSource(strings = {"&source=EUR", "&source=CAD", "&source=ILS", "&source=RUB"})
    public void liveSourceChangeTest(String source) {
        Response response = given().get(String.format("%s%s%s%s%s%s%s", ROOT, Constant.LIVE_ENDPOINT, ACCESS_KEY, CURRENCY_ENDPOINT, source, CURRENCY_ENDPOINT, source));
        response.then().statusCode(200)
                .body("success", equalTo(false))
                .body("error.info", notNullValue())
                .body("error.code", equalTo(105))
                .body(containsString("Access Restricted - Your current Subscription Plan does not support Source Currency Switching."));


    }

    //
    @Test
    public void historicalCorrectDateTest() {
        String root = String.format("%s%s%s%s", ROOT, HISTORICAL_ENDPOINT, ACCESS_KEY, HISTORICAL_DATE);
        Response response = given().get(root);
        response.then().statusCode(200).body("success", equalTo(true));
                response.then().body("source", equalTo("USD"))
                .body("date", equalTo("2020-01-01"));
    }


    @Test
    @DisplayName("Error 404 - resource does not exist")
    public void error404Test() {
        Response response = given().get(INCORRECT_LINK);
        response.then().statusCode(404);
    }


    @Test
    public void NegativeWithIncorrectKey() {
        String stringInvalidKey = String.format("%s%s%s", ROOT, Constant.LIVE_ENDPOINT, INVALID_KEY);
        Response response = given().get(stringInvalidKey);
        response.then().statusCode(200);
        response.then().body("success", equalTo(false))
                .body("error.code", equalTo(101))
                .body("error.type", equalTo("invalid_access_key"));


    }

    @Test
    @DisplayName("Error 202 - non-existent API function")
    public void error202Test() {
        Response response = given().get(ROOT + Constant.LIVE_ENDPOINT + ACCESS_KEY + "&currencies=RUS");
        response.then().statusCode(200);
        response.then().body("success", equalTo(false))
                .body("error.code", equalTo(202));


    }

    @Test
    @DisplayName("Error 105 - subscription plan does not support the endpoint")
    public void error105Test() {
        String incorrectEndpoint = String.format("%s%s%s%s", ROOT, Constant.LIVE_ENDPOINT, ACCESS_KEY, ENDPOINT_OUT_SUBSCRIPTION);
        Response response = given().get(incorrectEndpoint);
        response.then().statusCode(200);
        response.then().body("success", equalTo(false));
        response.then().assertThat().body("error.code", equalTo(105));
    }

    @Test
    @DisplayName("Error 201 - invalid Source")
    public void error201Test() {
        Response response = given().get(ROOT + Constant.LIVE_ENDPOINT + ACCESS_KEY + "&currencies=EUR,GBP,CAD&source=RUS&format=1");
        response.then().statusCode(200);
        response.then().body("success", equalTo(false));
        response.then().assertThat().body("error.code", equalTo(201));
    }


}
