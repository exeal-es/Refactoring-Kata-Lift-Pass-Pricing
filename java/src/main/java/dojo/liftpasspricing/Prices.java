package dojo.liftpasspricing;

import static dojo.liftpasspricing.StayCalculator.calculateCost;
import static spark.Spark.after;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.put;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Prices {

  public static Connection createApp() throws SQLException {

    final Connection connection =
        DriverManager.getConnection("jdbc:mysql://localhost:3306/lift_pass", "root", "mysql");

    port(4567);

    put(
        "/prices",
        (req, res) -> {
          int liftPassCost = Integer.parseInt(req.queryParams("cost"));
          String liftPassType = req.queryParams("type");

          try (PreparedStatement stmt =
              connection.prepareStatement( //
                  "INSERT INTO base_price (type, cost) VALUES (?, ?) "
                      + //
                      "ON DUPLICATE KEY UPDATE cost = ?")) {
            stmt.setString(1, liftPassType);
            stmt.setInt(2, liftPassCost);
            stmt.setInt(3, liftPassCost);
            stmt.execute();
          }

          return "";
        });

    get(
        "/prices",
        (req, res) -> {
          final Integer age =
              req.queryParams("age") != null ? Integer.valueOf(req.queryParams("age")) : null;

          try (PreparedStatement costStmt =
              connection.prepareStatement( //
                  "SELECT cost FROM base_price "
                      + //
                      "WHERE type = ?")) {
            String stayType = req.queryParams("type");
            costStmt.setString(1, stayType);
            try (ResultSet result = costStmt.executeQuery()) {
              boolean hasNextResult = result.next();

              int baseCost = hasNextResult ? result.getInt("cost") : 0;
              String date = req.queryParams("date");
              return calculateCost(age, connection, baseCost, date, stayType);
            }
          }
        });

    after(
        (req, res) -> {
          res.type("application/json");
        });

    return connection;
  }

}
