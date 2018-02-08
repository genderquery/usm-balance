package com.github.genderquery.usmbalance.widget;

import android.content.Context;
import android.support.annotation.NonNull;

public class AppWidgetUtils {

  private AppWidgetUtils() {
    // prevent instantiation
  }

  /**
   * Updates all of the app widgets.
   */
  public static void updateWidgets(@NonNull Context context) {
    LargeWidget.updateWidgets(context);
    SmallWidget.updateWidgets(context);
  }
}
