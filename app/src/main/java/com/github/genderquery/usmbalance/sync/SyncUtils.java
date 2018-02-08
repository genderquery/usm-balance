package com.github.genderquery.usmbalance.sync;

import android.content.Context;
import com.github.genderquery.usmbalance.data.Preferences;
import com.github.genderquery.usmbalance.data.UsmBalance;
import com.github.genderquery.usmbalance.data.UsmLine;
import com.github.genderquery.usmbalance.widget.AppWidgetUtils;
import java.io.IOException;
import java.util.ArrayList;

public class SyncUtils {

  private static final String TAG = "SyncUtils";

  public synchronized static ArrayList<UsmLine> getLines(Context context) throws IOException {
    String username = Preferences.getUsername(context);
    String password = Preferences.getPassword(context);
    UsmApi.login(username, password);
    return UsmApi.getLines();
  }

  public synchronized static void syncUsage(Context context) throws IOException {
    String username = Preferences.getUsername(context);
    String password = Preferences.getPassword(context);
    String lineId = Preferences.getLineId(context);

    UsmApi.login(username, password);
    UsmBalance balance = UsmApi.getBalance(lineId);
    Preferences.setBalance(context, balance);
    AppWidgetUtils.updateWidgets(context);
  }

}
