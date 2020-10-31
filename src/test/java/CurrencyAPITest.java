
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.*;

public class CurrencyAPITest {

    final static String ROOT = "http://api.currencylayer.com";
    final static String LIVE_ENDPOINT = "/live?access_key=";
    final static String HISTORICAL_ENDPOINT = "/historical?access_key=";
    final static String ACCESS_KEY = "11da75e340ea42f1f834acd9779d6af9";
    final static String CURRENCY_PAIR1 = "USDAED";
    final static String CURRENCY_PAIR2 = "USDNAD";
    final static String CURRENCY_PAIR3 = "USDPEN";
    final static String CURRENCY_PAIR4 = "USDLYD";
    final static String CURRENCY_PAIR5 = "USDEUR";
    final static String CURRENCY_PAIR6 = "USDCAD";
    final static String CURRENCY_PAIR7 = "USDILS";
    final static String CURRENCY_PAIR8 = "USDRUB";
    public static final String DEFAULT_CURRENCY = "USD";
    final static String CURRENCY_ENDPOINT = "&currencies=";
    final static String VALUE1 = "EUR";
    final static String VALUE2 = "CAD";
    final static String VALUE3 = "EUR";
    final static String VALUE4 = "ILS";
    final static String VALUE5 = "RUB";
    final static String HISTORICAL_DATE = "&date=2020-01-01";
    final static String INCORRECT_LINK = "http://currencylayer.com/live?access_key=11da75e340ea42f1f834acd9779d6af9";
    final static String INVALID_KEY = "11da75e340ea42f1f834acd9779d6af9oooss";
    final static String ENDPOINT_OUT_SUBSCRIPTION = "&currencies=EUR,GBP,CAD,PL&source=RUB&format=1";


    @ParameterizedTest
    @ValueSource(strings = {CURRENCY_PAIR1, CURRENCY_PAIR2, CURRENCY_PAIR3, CURRENCY_PAIR4, CURRENCY_PAIR5, CURRENCY_PAIR6, CURRENCY_PAIR7, CURRENCY_PAIR8})
    public void SanityDTestWithCorrectKey(String value) {
        Response response = given().get(String.format("%s%s%s", ROOT, LIVE_ENDPOINT, ACCESS_KEY));
        response.then().statusCode(200);
        response.then().body("success", equalTo(true))
                .body(containsString(value));
    }

    @Test
    public void noAccessKeyTest() {
        given().get(String.format("%s%s", ROOT, LIVE_ENDPOINT)).
                then().statusCode(200).
                and().body("success", equalTo(false)).
                and().body("error.code", equalTo(101)).
                and().body("error.type", equalTo("missing_access_key"));
    }


    @ParameterizedTest
    @ValueSource(strings = {VALUE1, VALUE2, VALUE3, VALUE4, VALUE5})
    public void getCurrencyTest(String curr) {
        Response response = given().get(String.format("%s%s%s%s%s", ROOT, LIVE_ENDPOINT, ACCESS_KEY, CURRENCY_ENDPOINT, curr));
        System.out.println(response.asString());
        response.then().statusCode(200);
        response.then().body("success", equalTo(true))
                .body("terms", notNullValue())
                .body("privacy", notNullValue())
                .body("timestamp", notNullValue())
                .body("source", equalTo(DEFAULT_CURRENCY))
                .body(String.format("quotes.%s%s", DEFAULT_CURRENCY, curr), notNullValue());
    }


    @ParameterizedTest
    @DisplayName("The current Subscription Plan does not support Source Currency Switching")
    @ValueSource(strings = {"&source=EUR", "&source=CAD", "&source=ILS", "&source=RUB"})
    public void liveSourceChangeTest(String source) {
        Response response = given().get(String.format("%s%s%s%s%s%s%s", ROOT, LIVE_ENDPOINT, ACCESS_KEY, CURRENCY_ENDPOINT, source, CURRENCY_ENDPOINT, source));
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
        response.then().statusCode(200).body("success", equalTo(true))
                .body("source", equalTo("USD"))
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
        String stringInvalidKey = String.format("%s%s%s", ROOT, LIVE_ENDPOINT, INVALID_KEY);
        Response response = given().get(stringInvalidKey);
        response.then().statusCode(200);
        response.then().body("success", equalTo(false))
                .body("error.code", equalTo(101))
                .body("error.type", equalTo("invalid_access_key"));


    }

    @Test
    @DisplayName("Error 202 - non-existent API function")
    public void error202Test() {
        Response response = given().get(ROOT + LIVE_ENDPOINT + ACCESS_KEY + "&currencies=RUS");
        response.then().statusCode(200);
        response.then().body("success", equalTo(false))
                .body("error.code", equalTo(202));


    }

    @Test
    @DisplayName("Error 105 - subscription plan does not support the endpoint")
    public void error105Test(){
        String incorrectEndpoint = String.format("%s%s%s%s", ROOT, LIVE_ENDPOINT, ACCESS_KEY, ENDPOINT_OUT_SUBSCRIPTION);
        Response response = given().get(incorrectEndpoint);
        response.then().statusCode(200);
        response.then().body("success", equalTo(false));
        response.then().assertThat().body("error.code", equalTo(105));
    }

    @Test
    @DisplayName("Error 201 - invalid Source")
    public void error201Test(){
        Response response = given().get(ROOT + LIVE_ENDPOINT+ACCESS_KEY + "&currencies=EUR,GBP,CAD&source=RUS&format=1");
        response.then().statusCode(200);
        response.then().body("success", equalTo(false));
        response.then().assertThat().body("error.code", equalTo(201));
    }


}
