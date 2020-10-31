package consts;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

import static consts.Constant.*;
import static consts.Constant.ROOT;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class ApiMethods {
    public static Response getCurrency(String value){
        return given().get(String.format("%s%s%s%s%s", ROOT, LIVE_ENDPOINT, ACCESS_KEY, CURRENCY_ENDPOINT, value));

    }
    public static Response noAccessKey() {
        return given().get(String.format("%s%s", ROOT, LIVE_ENDPOINT));
    }


    public static boolean StatusCod(String path) {
       boolean code = given().get(path).then().body(notNullValue()).extract().path("success");
        return code;
    }

    public static Response SanityWithCorrectKey() {
        return given().get(String.format("%s%s%s", ROOT, LIVE_ENDPOINT, ACCESS_KEY));
    }
}
