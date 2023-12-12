import com.shaft.driver.SHAFT;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RestfulBookerApisTest {
    SHAFT.API api;
    public static String Tok;
    public static String id;

    @Test
    public void agetTokenTest() {
        String tokenBody = """
                {
                    "username" : "admin",
                    "password" : "password123"
                }
                """;
        api.post("/auth")
                .setContentType(ContentType.JSON)
                .setRequestBody(tokenBody)
                .setTargetStatusCode(200)
                .perform();

        api.assertThatResponse().body().contains("\"token\":").perform();
         Tok = api.getResponseJSONValue("token");
    }


    @Test
    public void createBookingTest() {
        String createBookingBody = """
                {
                    "firstname" : "Mahmoud",
                    "lastname" : "ElSharkawy",
                    "totalprice" : 111,
                    "depositpaid" : true,
                    "bookingdates" : {
                        "checkin" : "2023-01-01",
                        "checkout" : "2024-01-01"
                    },
                    "additionalneeds" : "Hot Chocolate"
                }
                """;
        api.post("/booking")
                .setContentType(ContentType.JSON)
                .setRequestBody(createBookingBody)
                .setTargetStatusCode(200)
                .perform();

        api.verifyThatResponse().extractedJsonValue("booking.lastname").isEqualTo("ElSharkawy").perform();
        api.verifyThatResponse().extractedJsonValue("booking.additionalneeds").isEqualTo("Hot Chocolate").perform();
        api.verifyThatResponse().body().contains("\"bookingid\":").perform();
        id = api.getResponseJSONValue("bookingid");


    }

    @Test
    public void deleteBookingTest() {


        api.delete("/booking/" + id)
                .setContentType(ContentType.JSON)
                .addHeader("Cookie", "token="+Tok)
                .setTargetStatusCode(201)
                .perform();

       api.verifyThatResponse().body().contains("Created").perform();

    }


    @BeforeClass
    public void beforeClass() {
        api = new SHAFT.API("https://restful-booker.herokuapp.com");
    }

}