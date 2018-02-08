package com.github.genderquery.usmbalance.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import com.github.genderquery.usmbalance.R;
import com.github.genderquery.usmbalance.data.Preferences;

public class SplashActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    Intent intent;
    if (Preferences.isLoginRequired(this)) {
      intent = new Intent(this, LoginActivity.class);
    } else {
      intent = new Intent(this, BalanceActivity.class);
    }

    startActivity(intent);
    finish();
  }
}
