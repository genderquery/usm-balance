package com.github.genderquery.usmbalance.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import com.github.genderquery.usmbalance.R;

public class NotificationUtils {

  private static final String CHANNEL_ID_AUTH = "auth";
  private static final int NOTIFICATION_ID_AUTH = 1;

  private NotificationUtils() {
    // prevent instantiation
  }

  /**
   * Shows a notification for authorization issues encountered when running background sync jobs.
   */
  public static void notifyForAuthorization(@NonNull Context context) {
    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID_AUTH,
          context.getString(R.string.channel_auth),
          NotificationManager.IMPORTANCE_DEFAULT);
      notificationManager.createNotificationChannel(notificationChannel);
    }

    Intent loginIntent = new Intent(context, LoginActivity.class);
    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent pendingIntent =
        PendingIntent.getActivity(context, 0, loginIntent, PendingIntent.FLAG_ONE_SHOT);

    Notification notification = new Builder(context, CHANNEL_ID_AUTH)
        .setCategory(NotificationCompat.CATEGORY_ERROR)
        .setSmallIcon(R.drawable.ic_warning)
        .setContentTitle(context.getString(R.string.notif_title_auth_error))
        .setContentText(context.getString(R.string.notif_content_auth_error))
        .setContentIntent(pendingIntent)
        .build();

    notificationManager.notify(NOTIFICATION_ID_AUTH, notification);
  }
}
