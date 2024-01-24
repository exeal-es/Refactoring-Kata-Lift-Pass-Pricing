package dojo.liftpasspricing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HolidayRepository {

  private final Connection connection;

  public HolidayRepository(Connection connection) {

    this.connection = connection;
  }

  public List<Date> getHolidays() throws SQLException {
    List<Date> holidaysList = new ArrayList<Date>();
    try (PreparedStatement holidayStmt =
        connection.prepareStatement( //
            "SELECT * FROM holidays")) {
      try (ResultSet holidays = holidayStmt.executeQuery()) {

        while (holidays.next()) {
          Date holiday = holidays.getDate("holiday");
          holidaysList.add(holiday);
        }
      }
    }
    return holidaysList;
  }
}
