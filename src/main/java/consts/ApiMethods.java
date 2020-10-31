package consts;

import io.restassured.response.Response;

import static consts.Constant.*;
import static consts.Constant.ROOT;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class ApiMethods {
    public static Response getCurrency(String value){
        Response response = given().get(String.format("%s%s%s%s%s", ROOT, Constant.LIVE_ENDPOINT, ACCESS_KEY, CURRENCY_ENDPOINT, value));
        return response;

    }
    public static Response noAccessKey() {
        Response response = given().get(String.format("%s%s", ROOT, Constant.LIVE_ENDPOINT));
        return response;
    }


    public static boolean StatusCod() {
        Response response = null;
        response.then().statusCode(200).body("success", equalTo(true));
        return true;
    }

    public static Response SanityWithCorrectKey() {
        Response response = given().get(String.format("%s%s%s", ROOT, Constant.LIVE_ENDPOINT, ACCESS_KEY));
        return response;
    }
}
