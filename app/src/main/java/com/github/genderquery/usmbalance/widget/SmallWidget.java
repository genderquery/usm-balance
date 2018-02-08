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

public class SmallWidget extends AppWidgetProvider {

  private static final String TAG = "SmallWidget";

  /**
   * Updates all of the small app widgets.
   */
  public static void updateWidgets(Context context) {
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
    ComponentName componentName = new ComponentName(context, SmallWidget.class);
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

    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_small);
    views.setTextViewText(R.id.talk_text, Integer.toString(balance.talkRemaining));
    views.setTextViewText(R.id.text_text, Integer.toString(balance.textRemaining));
    views.setTextViewText(R.id.data_text, Integer.toString(balance.dataRemaining));

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

