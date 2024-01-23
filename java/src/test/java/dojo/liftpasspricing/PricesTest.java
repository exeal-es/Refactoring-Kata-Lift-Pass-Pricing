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
    public void shouldReturnFreeCostForAgeLessThan6() {
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
    public void shouldReturnCost8ForAge65AndTypeNight() {
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
    public void shouldReturnCost19ForAgeLessThan64AndTypeNight() {
        JsonPath response = RestAssured.
            given().
            port(4567).
            when().
            // construct some proper url parameters
                get("/prices?type=night&age=63").
            then().
            assertThat().
            statusCode(200).
            assertThat().
            contentType("application/json").
            extract().jsonPath();

        assertEquals(19, response.getInt("cost"));
    }

    @Test
    public void shouldReturnCost0ForAgeLessThan6AndTypeNight() {
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
    public void shouldReturnCost25ForAge14AndType1Jour() {
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

}
