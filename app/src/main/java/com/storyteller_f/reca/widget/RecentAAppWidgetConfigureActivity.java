package com.storyteller_f.reca.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.storyteller_f.reca.WidgetConfig;
import com.storyteller_f.reca.databinding.RecentAAppWidgetConfigureBinding;

/**
 * The configuration screen for the {@link RecentAAppWidget RecentAAppWidget} AppWidget.
 */
public class RecentAAppWidgetConfigureActivity extends Activity {
    private static final String TAG = "RecentAAppWidgetConfig";
    private static final String PREFS_NAME = "com.storyteller_f.reca.widget.RecentAAppWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    EditText colCountEdit;
    EditText rowCountEdit;
    private RecentAAppWidgetConfigureBinding binding;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = RecentAAppWidgetConfigureActivity.this;

            String colCount = binding.inputCol.getText().toString().trim();
            if (colCount.length() <= 0 && isNotNumber(colCount)) {
                return;
            }
            String rowCount = binding.inputRow.getText().toString().trim();
            if (rowCount.length() <= 0 && isNotNumber(rowCount)) {
                return;
            }
            saveTitlePref(context, mAppWidgetId, colCount, rowCount, binding.switchShow.isChecked());

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RecentAAppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public RecentAAppWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String colCount, String rowCount, boolean checked) {
        Log.d(TAG, "saveTitlePref() called with: context = [" + context + "], appWidgetId = [" + appWidgetId + "], colCount = [" + colCount + "], rowCount = [" + rowCount + "]");
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, colCount + "-" + rowCount + "-" + checked);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static WidgetConfig loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            String[] split = titleValue.split("-");
            if (split.length == 3) {
                return WidgetConfig.getConfig(split);
            } else return new WidgetConfig(2, 2);

        } else {
            return new WidgetConfig(2, 2);
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    private boolean isNotNumber(String string) {
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c <= '9' && c >= '0') {
                continue;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        binding = RecentAAppWidgetConfigureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        colCountEdit = binding.inputCol;
        rowCountEdit = binding.inputRow;
        binding.addButton.setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        WidgetConfig config = loadTitlePref(RecentAAppWidgetConfigureActivity.this, mAppWidgetId);
        binding.inputCol.setText(String.valueOf(config.col));
        binding.inputRow.setText(String.valueOf(config.row));
        binding.switchShow.setChecked(config.isUsedTime);
    }
}