package dojo.liftpasspricing;

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
import spark.Request;

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
            costStmt.setString(1, req.queryParams("type"));
            try (ResultSet result = costStmt.executeQuery()) {
              boolean hasNextResult = result.next();

              int baseCost = hasNextResult ? result.getInt("cost") : 0;
              String date = req.queryParams("date");
              return calculateCost(req, age, connection, baseCost, date);
            }
          }
        });

    after(
        (req, res) -> {
          res.type("application/json");
        });

    return connection;
  }

  private static String calculateCost(
      Request req, Integer age, Connection connection, int baseCost, String date)
      throws ParseException, SQLException {
    if (isChild(age)) {
      return buildCost(0);
    }

    if (!req.queryParams("type").equals("night")) {
      return calculateOneJourCost(req, connection, age, baseCost, date);
    }
    return calculateNightCost(age, baseCost);
  }

  private static String calculateOneJourCost(
      Request req, Connection connection, Integer age, int baseCost, String date)
      throws ParseException, SQLException {
    DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

    int reduction = calculateReduction(req, isoFormat, isHoliday(connection, isoFormat,
                                                                 date));

    // TODO apply reduction for others
    if (isTeenager(age)) {
      return buildCost((int) Math.ceil(baseCost * .7));
    }
    if (age == null) {
      double cost = baseCost * (1 - reduction / 100.0);
      return buildCost((int) Math.ceil(cost));
    }
    if (isSenior(age)) {
      double cost = baseCost * .75 * (1 - reduction / 100.0);
      return buildCost((int) Math.ceil(cost));
    }
    double cost = baseCost * (1 - reduction / 100.0);
    return buildCost((int) Math.ceil(cost));
  }

  private static String calculateNightCost(Integer age, int baseCost) throws SQLException {
    if (age == null) {
      return buildCost(0);
    }
    if (isSenior(age)) {
      return buildCost((int) Math.ceil(baseCost * .4));
    }
    return buildCost(baseCost);
  }

  private static boolean isTeenager(Integer age) {
    return age != null && age < 15;
  }

  private static boolean isChild(Integer age) {
    return age != null && age < 6;
  }

  private static boolean isHoliday(Connection connection, DateFormat isoFormat, String date)
      throws SQLException, ParseException {
    boolean isHoliday = false;
    try (PreparedStatement holidayStmt =
        connection.prepareStatement( //
            "SELECT * FROM holidays")) {
      try (ResultSet holidays = holidayStmt.executeQuery()) {

        while (holidays.next()) {
          Date holiday = holidays.getDate("holiday");
          if (date != null) {
            Date d = isoFormat.parse(date);
            if (d.getYear() == holiday.getYear()
                && //
                d.getMonth() == holiday.getMonth()
                && //
                d.getDate() == holiday.getDate()) {
              isHoliday = true;
            }
          }
        }
      }
    }
    return isHoliday;
  }

  private static int calculateReduction(Request req, DateFormat isoFormat, boolean isHoliday)
      throws ParseException {
    int reduction = 0;
    if (req.queryParams("date") != null) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(isoFormat.parse(req.queryParams("date")));
      if (!isHoliday && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
        reduction = 35;
      }
    }
    return reduction;
  }

  private static boolean isSenior(Integer age) {
    return age > 64;
  }

  private static String buildCost(int cost) {
    return "{ \"cost\": " + cost + "}";
  }
}
