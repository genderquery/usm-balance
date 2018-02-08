package com.github.genderquery.usmbalance.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.github.genderquery.usmbalance.R;
import com.github.genderquery.usmbalance.data.Preferences;
import com.github.genderquery.usmbalance.data.UsmLine;
import com.github.genderquery.usmbalance.sync.SyncJobService;
import com.github.genderquery.usmbalance.sync.SyncUtils;
import com.github.genderquery.usmbalance.sync.UsmApi.HttpUnauthorizedException;
import com.github.genderquery.usmbalance.ui.ChooseLineDialogFragment.ChooseLineDialogListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import org.jsoup.HttpStatusException;

public class LoginActivity extends AppCompatActivity implements ChooseLineDialogListener {

  public static final String ACTION_REAUTH = "com.github.genderquery.usmbalance.action.REAUTH";
  public static final String ACTION_LOGOUT = "com.github.genderquery.usmbalance.action.LOGOUT";

  private EditText usernameEditText;
  private EditText passwordEditText;
  private Button loginButton;
  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    usernameEditText = findViewById(R.id.username);
    passwordEditText = findViewById(R.id.password);
    loginButton = findViewById(R.id.login_button);
    progressBar = findViewById(R.id.progress);

    Intent intent = getIntent();
    String action = intent.getAction();
    if (ACTION_LOGOUT.equals(action)) {
      logout();
    } else if (ACTION_REAUTH.equals(action)) {
      // TODO show a message?
    }

    // Fill-in username if stored
    String username = Preferences.getUsername(this);
    if (!username.isEmpty()) {
      usernameEditText.setText(username);
      passwordEditText.requestFocus();
    }

    loginButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        attemptLogin();
      }
    });
  }

  private void logout() {
    Preferences.clear(this);
    SyncJobService.cancel(this);
  }

  private void attemptLogin() {
    new GetLinesAsyncTask(this).execute();
  }

  private void saveCredentials() {
    String username = usernameEditText.getText().toString();
    String password = passwordEditText.getText().toString();
    Preferences.setCredentials(this, username, password);
  }

  private void setUiLoading(boolean isLoading) {
    if (isLoading) {
      usernameEditText.setFocusable(false);
      passwordEditText.setFocusable(false);
      loginButton.setVisibility(View.GONE);
      progressBar.setVisibility(View.VISIBLE);
    } else {
      usernameEditText.setFocusableInTouchMode(true);
      passwordEditText.setFocusableInTouchMode(true);
      loginButton.setVisibility(View.VISIBLE);
      progressBar.setVisibility(View.GONE);
    }
  }

  private void showChooseLineDialog(ArrayList<UsmLine> lines) {
    ChooseLineDialogFragment dialog = ChooseLineDialogFragment.createInstance(lines);
    dialog.show(getSupportFragmentManager(), "choose-line-dialog");
  }

  @Override
  public void onLineSelected(UsmLine line) {
    Preferences.setLineId(this, line.id);
    SyncJobService.schedule(this);
    Intent intent = new Intent(this, BalanceActivity.class);
    intent.setAction(BalanceActivity.ACTION_REFRESH);
    startActivity(intent);
    finish();
  }

  private static class GetLinesAsyncTask extends AsyncTask<Void, Void, ArrayList<UsmLine>> {

    private static final String TAG = "GetLinesAsyncTask";

    private final WeakReference<LoginActivity> loginActivityWeakReference;
    private Throwable error;

    GetLinesAsyncTask(LoginActivity loginActivity) {
      loginActivityWeakReference = new WeakReference<>(loginActivity);
    }

    @Override
    protected void onPreExecute() {
      LoginActivity loginActivity = loginActivityWeakReference.get();
      loginActivity.setUiLoading(true);
      loginActivity.saveCredentials();
    }

    @Override
    protected ArrayList<UsmLine> doInBackground(Void... voids) {
      LoginActivity loginActivity = loginActivityWeakReference.get();
      try {
        return SyncUtils.getLines(loginActivity);
      } catch (Throwable t) {
        error = t;
        return null;
      }
    }

    @Override
    protected void onPostExecute(ArrayList<UsmLine> lines) {
      LoginActivity loginActivity = loginActivityWeakReference.get();
      loginActivity.setUiLoading(false);

      if (error != null) {
        if (error instanceof HttpUnauthorizedException) {
          AlertDialogUtils.showSimpleAlertDialog(loginActivity,
              R.string.login_error_invalid, R.string.dialog_try_again);

          loginActivity.passwordEditText.setText("");
          loginActivity.passwordEditText.requestFocus();

        } else if (error instanceof HttpStatusException) {
          Log.d(TAG, error.getLocalizedMessage(), error);
          AlertDialogUtils.showSimpleAlertDialog(loginActivity,
              R.string.login_error_server, R.string.dialog_try_again);

        } else {
          Log.e(TAG, error.getLocalizedMessage(), error);
          AlertDialogUtils.showSimpleAlertDialog(loginActivity,
              R.string.login_error_unknown, R.string.dialog_try_again);
        }

        return;
      }

      if (lines.size() == 0) {
        AlertDialogUtils.showSimpleAlertDialog(loginActivity,
            R.string.login_error_no_lines, R.string.dialog_ok);
        return;
      }

      loginActivity.showChooseLineDialog(lines);
    }
  }
}
