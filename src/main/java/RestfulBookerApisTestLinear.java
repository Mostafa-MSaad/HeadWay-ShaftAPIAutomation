import com.shaft.driver.SHAFT;
        import io.restassured.http.ContentType;
        import org.testng.annotations.BeforeClass;
        import org.testng.annotations.Test;

public class RestfulBookerApisTestLinear {
    private SHAFT.API api;

    // Status Codes
    private final int success_statusCode = 200;
    private final int successDelete_statusCode = 201;


    // Services names
    private final String authentication_serviceName = "/auth";
    private final String booking_serviceName = "/booking";


    //////////// Tests \\\\\\\\\\\\
    @Test
    public void createBookingTest() {
        createBooking("Mahmoud", "ElSharkawy", "Metdla3a");
        validateThatTheBookingIsCreated("Mahmoud", "ElSharkawy", "Metdla3a");
    }

    @Test(dependsOnMethods = {"createBookingTest"})
    public void deleteBookingTest() {
        deleteBooking(getBookingId("Mahmoud", "ElSharkawy"));
        validateThatTheBookingDeleted();
    }


    /////////// Configurations \\\\\\\\\\\\\\\
    @BeforeClass
    public void beforeClass() {
        api = new SHAFT.API("https://restful-booker.herokuapp.com");
        login("admin", "password123");
    }



    ////////////////// Business Methods | Actions \\\\\\\\\\\\\\\\\\\\\
    public void login(String username, String password) {
        String tokenBody = """
                {
                    "username" : "{USERNAME}",
                    "password" : "{PASSWORD}"
                }
                """
                .replace("{USERNAME}", username)
                .replace("{PASSWORD}", password);

        api.post(authentication_serviceName)
                .setContentType(ContentType.JSON)
                .setRequestBody(tokenBody)
                .setTargetStatusCode(success_statusCode)
                .perform();

        String token = api.getResponseJSONValue("token");

        api.addHeader("Cookie", "token=" + token);
    }

    public void createBooking(String firstName, String lastName, String additionalNeeds) {
        String createBookingBody = """
                {
                    "firstname" : "{FIRST_NAME}",
                    "lastname" : "{LAST_NAME}",
                    "totalprice" : 111,
                    "depositpaid" : true,
                    "bookingdates" : {
                        "checkin" : "2023-01-01",
                        "checkout" : "2024-01-01"
                    },
                    "additionalneeds" : "{ADDITIONAL_NEEDS}"
                }
                """
                .replace("{FIRST_NAME}", firstName)
                .replace("{LAST_NAME}", lastName)
                .replace("{ADDITIONAL_NEEDS}", additionalNeeds);

        api.post(booking_serviceName)
                .setContentType(ContentType.JSON)
                .setRequestBody(createBookingBody)
                .setTargetStatusCode(success_statusCode)
                .perform();
    }

    public String getBookingId(String firstName, String lastName) {
        api.get(booking_serviceName)
                .setUrlArguments("firstname=" + firstName + "&lastname=" + lastName)
                .perform();
        return api.getResponseJSONValue("$[0].bookingid");
    }

    private void deleteBooking(String bookingId) {
        api.delete("/booking/" + bookingId)
                .setContentType(ContentType.JSON)
                .setTargetStatusCode(201)
                .perform();
    }

    ////////// Validations \\\\\\\\\\\\\\\\\

    public void validateThatTheBookingIsCreated(String expectedFirstName, String expectedLastName, String expectedAdditionalNeeds) {
        api.verifyThatResponse().extractedJsonValue("booking.firstname").isEqualTo(expectedFirstName).perform();
        api.verifyThatResponse().extractedJsonValue("booking.lastname").isEqualTo(expectedLastName).perform();
        api.verifyThatResponse().extractedJsonValue("booking.additionalneeds").isEqualTo(expectedAdditionalNeeds).perform();
        api.verifyThatResponse().body().contains("\"bookingid\":").perform();
    }

    private void validateThatTheBookingDeleted() {
        api.verifyThatResponse().body().contains("Created").perform();
    }

}
