package dojo.liftpasspricing;


import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StayCalculator {

  private final HolidayRepository holidayRepository;

  public StayCalculator(HolidayRepository holidayRepository) {

    this.holidayRepository = holidayRepository;
  }

  public int calculateCost(Integer age, int baseCost, String date, String stayType)
      throws ParseException, SQLException {
    if (new Age(age).isChild()) {
      return 0;
    }

    if (!stayType.equals("night")) {
      return calculateOneJourCost(age, baseCost, date);
    }
    return calculateNightCost(age, baseCost);
  }

  private int calculateOneJourCost(Integer age, int baseCost, String date)
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
    if (new Age(age).isSenior()) {
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
    if (new Age(age).isSenior()) {
      return ((int) Math.ceil(baseCost * .4));
    }
    return baseCost;
  }

  private static boolean isTeenager(Integer age) {
    return age != null && age < 15;
  }

  private boolean isHoliday(DateFormat isoFormat, String date) throws SQLException, ParseException {
    boolean isHoliday = false;
    List<Date> holidaysList = holidayRepository.getHolidays();
    for (Date holiday : holidaysList) {
      isHoliday = isHoliday(isoFormat, date, holiday, isHoliday);
    }
    return isHoliday;
  }



  private static boolean isHoliday(
      DateFormat isoFormat, String date, Date holiday, boolean isHoliday) throws ParseException {
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

}
