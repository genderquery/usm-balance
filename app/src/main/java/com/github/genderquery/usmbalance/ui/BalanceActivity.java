package com.github.genderquery.usmbalance.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.github.genderquery.usmbalance.R;
import com.github.genderquery.usmbalance.data.Preferences;
import com.github.genderquery.usmbalance.data.UsmBalance;
import com.github.genderquery.usmbalance.sync.SyncUtils;
import com.github.genderquery.usmbalance.sync.UsmApi.HttpUnauthorizedException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BalanceActivity extends AppCompatActivity implements OnSharedPreferenceChangeListener {

  /**
   * Sync new balance data when starting activity.
   */
  public static final String ACTION_REFRESH = "com.github.genderquery.usmbalance.action.REFRESH";

  private static final String TAG = "BalanceActivity";

  private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
  private static final DateFormat dateTimeFormat =
      DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

  private TextView serviceTextView;
  private ProgressBar serviceProgressBar;
  private TextView daysLeftTextView;
  private TextView talkTextView;
  private ProgressBar talkProgressBar;
  private TextView textTextView;
  private ProgressBar textProgressBar;
  private TextView dataTextView;
  private ProgressBar dataProgressBar;
  private TextView lastUpdatedTextView;
  private SwipeRefreshLayout swipeRefreshLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_details);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    swipeRefreshLayout = findViewById(R.id.swipe_refresh);
    serviceTextView = findViewById(R.id.service);
    serviceProgressBar = findViewById(R.id.service_progress);
    daysLeftTextView = findViewById(R.id.days);
    talkTextView = findViewById(R.id.talk);
    talkProgressBar = findViewById(R.id.talk_progress);
    textTextView = findViewById(R.id.text);
    textProgressBar = findViewById(R.id.text_progress);
    dataTextView = findViewById(R.id.data);
    dataProgressBar = findViewById(R.id.data_progress);
    lastUpdatedTextView = findViewById(R.id.last_updated);

    // Require the user to login first
    if (Preferences.isLoginRequired(this)) {
      startActivity(new Intent(this, LoginActivity.class));
      finish();
    }

    swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
      @Override
      public void onRefresh() {
        refresh();
      }
    });

    Intent intent = getIntent();
    if (intent != null && ACTION_REFRESH.equals(intent.getAction())) {
      refresh();
    } else {
      updateUi();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    PreferenceManager.getDefaultSharedPreferences(this)
        .registerOnSharedPreferenceChangeListener(this);

    // balance may have been updated since the activity was last visible
    updateUi();
  }

  @Override
  protected void onPause() {
    super.onPause();
    // don't listen for changes if the activity isn't visible
    PreferenceManager.getDefaultSharedPreferences(this)
        .unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.details_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    switch (itemId) {
      case R.id.refresh:
        refresh();
        return true;
      case R.id.settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    // if the balance was updated by the service, update the UI
    updateUi();
  }

  /**
   * Updates the UI with the last stored balance data.
   */
  private void updateUi() {
    UsmBalance balance = Preferences.getBalance(this);
    long lastUpdated = Preferences.getLastUpdated(this);

    long now = Calendar.getInstance(Locale.US).getTimeInMillis();
    int daysLeft = (int) TimeUnit.MILLISECONDS.toDays(balance.serviceEndDate - now);

    Resources resources = getResources();
    serviceTextView.setText(getString(R.string.heading_service_ends_date,
        dateFormat.format(balance.serviceEndDate)));
    serviceProgressBar.setProgress(daysLeft);
    daysLeftTextView.setText(getString(R.string.service_days_remaining, daysLeft));

    talkTextView.setText(resources.getString(R.string.talk_remaining, balance.talkRemaining));
    talkProgressBar.setProgress(balance.talkPercent);

    textTextView.setText(resources.getString(R.string.text_remaining, balance.textRemaining));
    textProgressBar.setProgress(balance.textPercent);

    dataTextView.setText(resources.getString(R.string.data_remaining, balance.dataRemaining));
    dataProgressBar.setProgress(balance.dataPercent);

    lastUpdatedTextView.setText(getString(R.string.last_updated_datetime,
        dateTimeFormat.format(lastUpdated)));
  }

  /**
   * Sync new balance data.
   */
  private void refresh() {
    new SyncTask(this).execute();
  }

  private static class SyncTask extends AsyncTask<Void, Void, Void> {

    private final WeakReference<BalanceActivity> balanceActivityWeakReference;
    private Exception error;

    SyncTask(BalanceActivity balanceActivity) {
      balanceActivityWeakReference = new WeakReference<>(balanceActivity);
    }

    @Override
    protected void onPreExecute() {
      BalanceActivity balanceActivity = balanceActivityWeakReference.get();
      balanceActivity.swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    protected Void doInBackground(Void... unused) {
      BalanceActivity balanceActivity = balanceActivityWeakReference.get();
      try {
        SyncUtils.syncUsage(balanceActivity);
      } catch (Exception e) {
        error = e;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
      BalanceActivity balanceActivity = balanceActivityWeakReference.get();
      balanceActivity.swipeRefreshLayout.setRefreshing(false);

      if (error != null) {
        if (error instanceof HttpUnauthorizedException) {
          // Open the Login activity
          Intent intent = new Intent(balanceActivity, LoginActivity.class);
          intent.setAction(LoginActivity.ACTION_REAUTH);
          balanceActivity.startActivity(intent);
          balanceActivity.finish();
        } else {
          View contentView = balanceActivity.findViewById(android.R.id.content);
          Snackbar.make(contentView, R.string.login_error_unknown, Snackbar.LENGTH_LONG).show();
        }
      }

      balanceActivity.updateUi();
    }
  }
}
