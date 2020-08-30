package com.example.inappupdate;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class InAppUpdateModule extends ReactContextBaseJavaModule implements InstallStateUpdatedListener, LifecycleEventListener {
    private AppUpdateManager appUpdateManager;
    private static ReactApplicationContext reactContext;
    private static final int STALE_DAYS = 5;
    private static final int MY_REQUEST_CODE = 0;

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
            if (requestCode == MY_REQUEST_CODE) {
                if (resultCode != RESULT_OK) {
                    System.out.println("Update flow failed! Result code: " + resultCode);
                    // If the update is cancelled or fails,
                    // you can request to start the update again.
                }
            }
        }
    };

    InAppUpdateModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        reactContext.addActivityEventListener(mActivityEventListener);
        reactContext.addLifecycleEventListener(this);

    }

    @NonNull
    @Override
    public String getName() {
        return "InAppUpdate";
    }

    @ReactMethod
    public void checkUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(reactContext);
        appUpdateManager.registerListener(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.clientVersionStalenessDays() != null
                    && appUpdateInfo.clientVersionStalenessDays() > STALE_DAYS
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            reactContext.getCurrentActivity(),
                            MY_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }else{
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.FLEXIBLE,
                                reactContext.getCurrentActivity(),
                                MY_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }

        });

    }

    @Override
    public void onStateUpdate(InstallState state) {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate();
        }
    }

    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(Objects.requireNonNull(reactContext.getCurrentActivity()).findViewById(android.R.id.content).getRootView(),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(Color.GREEN);
        snackbar.show();
    }

    @Override
    public void onHostResume() {
        if (appUpdateManager != null) {
            appUpdateManager
                    .getAppUpdateInfo()
                    .addOnSuccessListener(
                            appUpdateInfo -> {
                                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                                    popupSnackbarForCompleteUpdate();
                                }
                                if (appUpdateInfo.updateAvailability()
                                        == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                    try {
                                        appUpdateManager.startUpdateFlowForResult(
                                                appUpdateInfo,
                                                AppUpdateType.IMMEDIATE,
                                                reactContext.getCurrentActivity(),
                                                MY_REQUEST_CODE);
                                    } catch (IntentSender.SendIntentException e) {
                                        e.printStackTrace();
                                    }
                                }

                            });
        }
    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        if (appUpdateManager != null) {
            appUpdateManager.unregisterListener(this);
        }
    }
}
