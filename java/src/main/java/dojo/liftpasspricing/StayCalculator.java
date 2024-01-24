package dojo.liftpasspricing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StayCalculator {

  private final Connection connection;

  public StayCalculator(Connection connection) {

    this.connection = connection;
  }

  public int calculateCost(Integer age, int baseCost, String date, String stayType)
      throws ParseException, SQLException {
    if (isChild(age)) {
      return 0;
    }

    if (!stayType.equals("night")) {
      return calculateOneJourCost(age, baseCost, date);
    }
    return calculateNightCost(age, baseCost);
  }

  private  int calculateOneJourCost(
      Integer age, int baseCost, String date)
      throws ParseException, SQLException {
    DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

    int reduction = calculateReduction(isoFormat, isHoliday(isoFormat, date), date);

    // TODO apply reduction for others
    if (isTeenager(age)) {
      return ((int) Math.ceil(baseCost * .7));
    }
    if (age == null) {
      double cost = baseCost * (1 - reduction / 100.0);
      return ((int) Math.ceil(cost));
    }
    if (isSenior(age)) {
      double cost = baseCost * .75 * (1 - reduction / 100.0);
      return ((int) Math.ceil(cost));
    }
    double cost = baseCost * (1 - reduction / 100.0);
    return ((int) Math.ceil(cost));
  }

  private static int calculateNightCost(Integer age, int baseCost) throws SQLException {
    if (age == null) {
      return 0;
    }
    if (isSenior(age)) {
      return ((int) Math.ceil(baseCost * .4));
    }
    return baseCost;
  }

  private static boolean isTeenager(Integer age) {
    return age != null && age < 15;
  }

  private static boolean isChild(Integer age) {
    return age != null && age < 6;
  }

  private boolean isHoliday(DateFormat isoFormat, String date)
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

  private static int calculateReduction(DateFormat isoFormat, boolean isHoliday, String date)
      throws ParseException {
    int reduction = 0;
    if (date != null) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(isoFormat.parse(date));
      if (!isHoliday && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
        reduction = 35;
      }
    }
    return reduction;
  }

  private static boolean isSenior(Integer age) {
    return age > 64;
  }


}
