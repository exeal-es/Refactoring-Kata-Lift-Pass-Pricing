package dojo.liftpasspricing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.*;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import spark.Spark;

public class PricesTest {

    private static Connection connection;

    @BeforeAll
    public static void createPrices() throws SQLException {
        connection = Prices.createApp();
    }

    @AfterAll
    public static void stopApplication() throws SQLException {
        Spark.stop();
        connection.close();
    }

    @Test
    public void shouldBeFreeForChildren() {
        JsonPath response = RestAssured.
            given().
            port(4567).
            when().
            // construct some proper url parameters
                get("/prices?age=4").
            then().
            assertThat().
            statusCode(200).
            assertThat().
            contentType("application/json").
            extract().jsonPath();

        assertEquals(0, response.getInt("cost"));
    }


    @Test
    public void shouldApply60PercentDiscountForNightStaysForSeniors() {
        JsonPath response = RestAssured.
            given().
                port(4567).
            when().
                // construct some proper url parameters
                get("/prices?type=night&age=65").
            then().
                assertThat().
                    statusCode(200).
                assertThat().
                    contentType("application/json").
            extract().jsonPath();

        assertEquals(8, response.getInt("cost"));
    }

    @Test
    public void shouldNotApplyDiscountForNightStaysForAdults() {
        JsonPath response = RestAssured.
            given().
            port(4567).
            when().
            // construct some proper url parameters
                get("/prices?type=night&age=40").
            then().
            assertThat().
            statusCode(200).
            assertThat().
            contentType("application/json").
            extract().jsonPath();

        assertEquals(19, response.getInt("cost"));
    }

    @Test
    public void shouldBeFreeForChildrenNightStays() {
        JsonPath response = RestAssured.
            given().
            port(4567).
            when().
            // construct some proper url parameters
                get("/prices?type=night&age=2").
            then().
            assertThat().
            statusCode(200).
            assertThat().
            contentType("application/json").
            extract().jsonPath();

        assertEquals(0, response.getInt("cost"));
    }


    @Test
    public void shouldApply30PercentDiscountForTeenagers() {
        JsonPath response = RestAssured.
            given().
            port(4567).
            when().
            // construct some proper url parameters
                get("/prices?age=14&type=1jour").
            then().
            assertThat().
            statusCode(200).
            assertThat().
            contentType("application/json").
            extract().jsonPath();

        assertEquals(25, response.getInt("cost"));
    }

    @Test
    public void shouldNotApplyDiscountForAdults() {
        JsonPath response = RestAssured.
            given().
            port(4567).
            when().
            // construct some proper url parameters
                get("/prices?age=30&type=1jour").
            then().
            assertThat().
            statusCode(200).
            assertThat().
            contentType("application/json").
            extract().jsonPath();

        assertEquals(35, response.getInt("cost"));
    }

    @Test
    public void shouldNotApplyDiscountIfNotDateOrAgeProvided() {
        JsonPath response = RestAssured.
            given().
            port(4567).
            when().
            // construct some proper url parameters
                get("/prices?type=1jour").
            then().
            assertThat().
            statusCode(200).
            assertThat().
            contentType("application/json").
            extract().jsonPath();

        assertEquals(35, response.getInt("cost"));
    }

    @Test
    public void shouldApply25PercentDiscountForSeniors() {
        JsonPath response = RestAssured.
            given().
            port(4567).
            when().
            // construct some proper url parameters
                get("/prices?age=70&type=1jour").
            then().
            assertThat().
            statusCode(200).
            assertThat().
            contentType("application/json").
            extract().jsonPath();

        assertEquals(27, response.getInt("cost"));
    }

    @Test
    public void shouldApplyReductionIfNotHolidaysAndItsMonday() {
        JsonPath response = RestAssured.
            given().
            port(4567).
            when().
            // construct some proper url parameters
                get("/prices?date=2024-01-22&type=1jour").
            then().
            assertThat().
            statusCode(200).
            assertThat().
            contentType("application/json").
            extract().jsonPath();

        assertEquals(23, response.getInt("cost"));
    }

    @Test
    public void shouldNotApplyReductionForHolidays() {
        JsonPath response = RestAssured.
            given().
            port(4567).
            when().
            // construct some proper url parameters
                get("/prices?date=2019-02-18&type=1jour").
            then().
            assertThat().
            statusCode(200).
            assertThat().
            contentType("application/json").
            extract().jsonPath();

        assertEquals(35, response.getInt("cost"));
    }

}
