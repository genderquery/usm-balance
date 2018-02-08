package com.github.genderquery.usmbalance.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public class AlertDialogUtils {

  private AlertDialogUtils() {
    // prevent instantiation
  }

  /**
   * Creates and shows a simple dialog for error or information messages.
   *
   * Shows a message and has a single button that dismisses the dialog.
   *
   * @param context calling context
   * @param messageId resource ID of message
   * @param buttonTextId resources ID of button text
   */
  public static void showSimpleAlertDialog(@NonNull Context context, @StringRes int messageId,
      @StringRes int buttonTextId) {
    showSimpleAlertDialog(context, context.getString(messageId), context.getString(buttonTextId));
  }

  /**
   * Creates and shows a simple dialog for error or information messages.
   *
   * Shows a message and has a single button that dismisses the dialog.
   *
   * @param context calling context
   * @param message message text
   * @param buttonText button text
   */
  public static void showSimpleAlertDialog(@NonNull Context context, String message,
      String buttonText) {
    new AlertDialog.Builder(context)
        .setMessage(message)
        .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        })
        .create()
        .show();
  }
}
