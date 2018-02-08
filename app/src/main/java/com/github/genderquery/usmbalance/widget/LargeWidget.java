package com.github.genderquery.usmbalance.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.github.genderquery.usmbalance.R;
import com.github.genderquery.usmbalance.data.Preferences;
import com.github.genderquery.usmbalance.data.UsmBalance;
import com.github.genderquery.usmbalance.ui.BalanceActivity;
import java.util.Calendar;

public class LargeWidget extends AppWidgetProvider {

  private static final String TAG = "LargeWidget";

  /**
   * Updates all of the large app widgets.
   */
  public static void updateWidgets(Context context) {
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
    ComponentName componentName = new ComponentName(context, LargeWidget.class);
    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
    updateAppWidgets(context, appWidgetManager, appWidgetIds);
  }

  static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager,
      int[] appWidgetIds) {
    for (int appWidgetId : appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId);
    }
  }

  static void updateAppWidget(Context context, AppWidgetManager widgetManager, int widgetId) {
    UsmBalance balance = Preferences.getBalance(context);
    long lastUpdated = Preferences.getLastUpdated(context);

    String formattedLastUpdated = com.github.genderquery.usmbalance.DateUtils
        .formatSameDayTime(lastUpdated,
            Calendar.getInstance().getTimeInMillis(), "MMM d", "h:mm a");

    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_large);
    views.setTextViewText(R.id.talk_text, Integer.toString(balance.talkRemaining));
    views.setTextViewText(R.id.text_text, Integer.toString(balance.textRemaining));
    views.setTextViewText(R.id.data_text, Integer.toString(balance.dataRemaining));
    views.setProgressBar(R.id.talk_progress, 100, balance.talkPercent, false);
    views.setProgressBar(R.id.text_progress, 100, balance.textPercent, false);
    views.setProgressBar(R.id.data_progress, 100, balance.dataPercent, false);
    views.setTextViewText(R.id.last_updated,
        context.getString(R.string.last_updated_date, formattedLastUpdated));

    Intent intent = new Intent(context, BalanceActivity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
    views.setOnClickPendingIntent(R.id.content, pendingIntent);

    widgetManager.updateAppWidget(widgetId, views);
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    updateAppWidgets(context, appWidgetManager, appWidgetIds);
  }
}

