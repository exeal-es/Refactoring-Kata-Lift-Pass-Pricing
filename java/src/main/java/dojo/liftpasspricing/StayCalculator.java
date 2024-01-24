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

  public Money calculateCost(Integer age, int baseCost, String date, String stayType)
      throws ParseException, SQLException {
    if (new Age(age).isChild()) {
      return new Money(0);
    }

    if (!new StayType(stayType).isNight()) {
      return calculateOneJourCost(age, baseCost, date);
    }
    return calculateNightCost(age, baseCost);
  }

  private Money calculateOneJourCost(Integer age, int baseCost, String date)
      throws ParseException, SQLException {
    DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

    int reduction = calculateReduction(isoFormat, isHoliday(isoFormat, date), date);

    // TODO apply reduction for others
    if (new Age(age).isTeenager()) {
      return new Money( baseCost * .7).roundUp();
    }
    if (new Age(age).isUnknown()) {
      double cost = baseCost * (1 - reduction / 100.0);
      return new Money(cost).roundUp();
    }
    if (new Age(age).isSenior()) {
      double cost = baseCost * .75 * (1 - reduction / 100.0);
      return new Money(cost).roundUp();
    }
    double cost = baseCost * (1 - reduction / 100.0);
    return new Money(cost).roundUp();
  }

  private static Money calculateNightCost(Integer age, int baseCost) {
    if (new Age(age).isSenior()) {
      return new Money(baseCost * .4).roundUp();
    }
    return new Money(baseCost);
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
