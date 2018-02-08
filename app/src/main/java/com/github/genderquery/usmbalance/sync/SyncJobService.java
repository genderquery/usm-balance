package com.github.genderquery.usmbalance.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.FirebaseJobDispatcher.ScheduleFailedException;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.github.genderquery.usmbalance.data.Preferences;
import com.github.genderquery.usmbalance.sync.UsmApi.HttpUnauthorizedException;
import com.github.genderquery.usmbalance.ui.NotificationUtils;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * A recurring job that syncs the balance data for the current account and line.
 */
public class SyncJobService extends JobService {

  private static final String TAG = "SyncJobService";
  private static final String JOB_TAG = "sync-job-service";

  private AsyncTask syncTask;

  /**
   * Schedule a recurring job to sync balance data. The job will persist across reboot and replace
   * any existing job. The frequency and constraints will be read from the stored Preferences.
   *
   * @param context will be used to retrieve preferences and create a dispatcher
   * @return the scheduled {@link Job}
   * @throws ScheduleFailedException thrown if the job could not be scheduled
   */
  public static Job schedule(@NonNull Context context) throws ScheduleFailedException {
    int syncFrequency = Preferences.getSyncFrequency(context);
    boolean syncUnmeteredOnly = Preferences.getSyncUnmeteredOnly(context);

    int periodSeconds = (int) TimeUnit.MINUTES.toSeconds(syncFrequency);
    int tolerance = (int) (periodSeconds * 0.15 + 0.5);

    FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

    Job job = dispatcher.newJobBuilder()
        .setService(SyncJobService.class)
        .setTag(JOB_TAG)
        .setReplaceCurrent(true)
        .setRecurring(true)
        .setLifetime(Lifetime.FOREVER)
        .setTrigger(Trigger.executionWindow(periodSeconds, periodSeconds + tolerance))
        .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
        .setConstraints(syncUnmeteredOnly
            ? Constraint.ON_UNMETERED_NETWORK
            : Constraint.ON_ANY_NETWORK)
        .build();
    dispatcher.mustSchedule(job);

    return job;
  }

  /**
   * Cancel a previously scheduled job, if it exists.
   * @param context
   */
  public static void cancel(@NonNull Context context) {
    FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
    dispatcher.cancel(JOB_TAG);
  }

  @Override
  public boolean onStartJob(JobParameters job) {
    syncTask = new SyncTask(this).execute(job);
    return true;
  }

  @Override
  public boolean onStopJob(JobParameters job) {
    if (syncTask != null) {
      syncTask.cancel(true);
    }
    return true;
  }

  private static class SyncTask extends AsyncTask<JobParameters, Void, JobParameters> {

    private final WeakReference<SyncJobService> syncJobServiceWeakReference;
    private Exception error;

    SyncTask(SyncJobService syncJobService) {
      syncJobServiceWeakReference = new WeakReference<>(syncJobService);
    }

    @Override
    protected JobParameters doInBackground(JobParameters... jobParameters) {
      SyncJobService syncJobService = syncJobServiceWeakReference.get();
      try {
        SyncUtils.syncUsage(syncJobService);
      } catch (Exception e) {
        error = e;
      }
      return jobParameters[0];
    }

    @Override
    protected void onPostExecute(JobParameters jobParameters) {
      SyncJobService syncJobService = syncJobServiceWeakReference.get();
      if (error != null) {
        if (error instanceof HttpUnauthorizedException) {
          // Let the user know there was an auth problem
          NotificationUtils.notifyForAuthorization(syncJobService);
        } else {
          Log.e(TAG, error.getLocalizedMessage(), error);
        }
      }
      syncJobService.jobFinished(jobParameters, false);
    }
  }
}
