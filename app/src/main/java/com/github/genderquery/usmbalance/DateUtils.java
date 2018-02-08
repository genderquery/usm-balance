package com.github.genderquery.usmbalance;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

  /**
   * Like {@link android.text.format.DateUtils#formatSameDayTime(long, long, int, int)}, but takes
   * format strings.
   *
   * @see SimpleDateFormat
   */
  public static String formatSameDayTime(long then, long now,
      String dateFormat, String timeFormat) {
    Calendar thenCal = Calendar.getInstance();
    thenCal.setTimeInMillis(then);
    Date thenDate = thenCal.getTime();
    Calendar nowCal = Calendar.getInstance();
    nowCal.setTimeInMillis(now);

    SimpleDateFormat simpleDateFormat;

    if (thenCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
        && thenCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH)
        && thenCal.get(Calendar.DAY_OF_MONTH) == nowCal.get(Calendar.DAY_OF_MONTH)) {
      simpleDateFormat = new SimpleDateFormat(timeFormat);
    } else {
      simpleDateFormat = new SimpleDateFormat(dateFormat);
    }

    return simpleDateFormat.format(thenDate);
  }

}
