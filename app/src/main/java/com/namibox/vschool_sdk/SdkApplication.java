package com.namibox.vschool_sdk;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import sdk.NamiboxSDK;
import java.lang.Thread.UncaughtExceptionHandler;

public class SdkApplication extends Application {

  private static final String TAG = "SdkApplication";

  @Override
  public void onCreate() {
    super.onCreate();
    NamiboxSDK.init(this,BuildConfig.DEBUG);
    initExceptionHandler();
    registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
      @Override
      public void onActivityCreated(@NonNull Activity activity,
          @Nullable Bundle savedInstanceState) {

      }

      @Override
      public void onActivityStarted(@NonNull Activity activity) {

      }

      @Override
      public void onActivityResumed(@NonNull Activity activity) {

      }

      @Override
      public void onActivityPaused(@NonNull Activity activity) {

      }

      @Override
      public void onActivityStopped(@NonNull Activity activity) {

      }

      @Override
      public void onActivitySaveInstanceState(@NonNull Activity activity,
          @NonNull Bundle outState) {

      }

      @Override
      public void onActivityDestroyed(@NonNull Activity activity) {

      }
    });
  }

  private void initExceptionHandler() {
    final UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
      StackTraceElement[] trace = e.getStackTrace();
      if (trace != null) {
        Log.e("Application", e.toString());
      }
      defaultHandler.uncaughtException(t, e);
    });
  }
}
