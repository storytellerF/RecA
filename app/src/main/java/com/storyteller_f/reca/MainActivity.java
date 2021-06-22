package com.storyteller_f.reca;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.storyteller_f.reca.databinding.ActivityMainBinding;
import com.storyteller_f.reca.vm.MainViewModel;
import com.storyteller_f.reca.widget.RecentAAppWidget;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AlertDialog.Builder builder;
    private ActivityMainBinding inflate;
    private MainViewModel model;

    public static void notifyWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, RecentAAppWidget.class));
        Log.i(TAG, "notifyWidget: " + Arrays.toString(appWidgetIds));
//        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.aList);
        for (int appWidgetId : appWidgetIds) {
            RecentAAppWidget.updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflate = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(inflate.getRoot());
        builder = new AlertDialog.Builder(this);
        model = new ViewModelProvider(this).get(MainViewModel.class);
        model.getPermission().observe(this, aBoolean -> {
            inflate.showPermission.setText(aBoolean.toString());
            inflate.buttonRequestPermission.setEnabled(!aBoolean);
            inflate.buttonForceRefresh.setEnabled(aBoolean);
        });
        inflate.buttonRequestPermission.setOnClickListener(v -> request());
        inflate.buttonForceRefresh.setOnClickListener(v -> notifyWidget(v.getContext()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUsagePermission();
        notifyWidget(this);
    }

    /**
     * 检查能够获得使用情况
     */
    private void checkUsagePermission() {
        boolean granted = usageIsGranted();
        model.setP(granted);
        if (granted) {
            return;
        }
        builder.setMessage("需要使用权限，才能获得最近应用").setPositiveButton("确认", (dialog, which) -> {
            Log.i(TAG, "onClick: 申请使用权限");
            request();
        }).show();
    }

    private void request() {
        Intent intent2 = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent2);
    }

    private boolean usageIsGranted() {
        AppOpsManager appOps = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
        int mode;
        if (appOps != null) {
            mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), this.getPackageName());
        } else
            mode = 0;
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}