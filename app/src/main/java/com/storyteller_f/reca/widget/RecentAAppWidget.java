package com.storyteller_f.reca.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.RemoteViews;

import com.storyteller_f.reca.MainActivity;
import com.storyteller_f.reca.R;
import com.storyteller_f.reca.WidgetConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link RecentAAppWidgetConfigureActivity RecentAAppWidgetConfigureActivity}
 */
public class RecentAAppWidget extends AppWidgetProvider {
    private static final String TAG = "RecentAAppWidget";
    private static final String action_click = "com.storyteller_f.widget.RecentAppEvent.Click";
    private static final String action_setting = "com.storyteller_f.widget.RecentAppEvent.Setting";
    private static final String action_refresh = "com.storyteller_f.widget.RecentAppEvent.Refresh";
    public static String extra_index = "com.storyteller_f_.widget.RecentAppEvent.index";
    public static String packageName = "com.storyteller_f_.widget.RecentAppEvent.package";

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       int appWidgetId) {
        Log.d(TAG, "updateAppWidget() called with: context = [" + context + "], appWidgetManager = [" + appWidgetManager + "], appWidgetId = [" + appWidgetId + "]");
        WidgetConfig widgetText = RecentAAppWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recent_a_app_widget);
        Intent intent = new Intent(context, RecentAppService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra("date", System.currentTimeMillis());
        intent.putExtra("total", widgetText.col * widgetText.row);
        intent.putExtra("isUsedTime",widgetText.isUsedTime);

        Uri parse = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME));
        Log.i(TAG, "updateAppWidget: uri:" + parse.getPath());
        intent.setData(parse);
        views.setRemoteAdapter(R.id.aList, intent);
        long l = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss SSS", Locale.CHINA);
        views.setTextViewText(R.id.updatedTime, simpleDateFormat.format(new Date(l)));
        /*设置按钮*/
        {
            Intent redrawIntent = new Intent(context, RecentAAppWidget.class).setAction(action_setting);
            redrawIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent redrawPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, redrawIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.buttonSetting, redrawPendingIntent);
        }
        /*重绘按钮*/
        {
            Intent redrawIntent = new Intent(context, RecentAAppWidget.class).setAction(action_refresh);
            redrawIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent redrawPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, redrawIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.buttonRefresh, redrawPendingIntent);
        }
        /*
        点击，传递appwidget id
         */
        {
            Intent clickAction = new Intent(context, RecentAAppWidget.class);
            clickAction.setAction(action_click);
            clickAction.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
//            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, clickAction, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.aList, toastPendingIntent);
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RecentAAppWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        Log.i(TAG, "onReceive: " + action);
        if (action != null) {
            switch (action) {
                case action_click:
                    Bundle extras = intent.getExtras();
                    String packageName = extras.getString(RecentAAppWidget.packageName, null);
                    Log.i(TAG, "onReceive: package:" + packageName);
                    if (packageName != null) {
                        //启动app
                        Intent start = context.getPackageManager().getLaunchIntentForPackage(packageName);
                        if (start != null) {
                            context.startActivity(start);
                        }
                    }
                    break;
                case action_setting:
                    Bundle bundle = new Bundle();
                    int intExtra = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                    bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, intExtra);
                    Intent start = new Intent(context, RecentAAppWidgetConfigureActivity.class);
                    start.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    start.putExtras(bundle);
                    context.startActivity(start);
                    break;
                case action_refresh:
                    MainActivity.notifyWidget(context);
                    break;
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}